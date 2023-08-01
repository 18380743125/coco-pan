package com.tangl.pan.server.modules.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tangl.pan.cache.core.constants.CacheConstants;
import com.tangl.pan.core.exception.TPanBusinessException;
import com.tangl.pan.core.exception.TPanFrameworkException;
import com.tangl.pan.core.response.ResponseCode;
import com.tangl.pan.core.utils.IdUtil;
import com.tangl.pan.core.utils.JwtUtil;
import com.tangl.pan.core.utils.PasswordUtil;
import com.tangl.pan.server.modules.file.constants.FileConstants;
import com.tangl.pan.server.modules.file.context.CreateFolderContext;
import com.tangl.pan.server.modules.file.service.IUserFileService;
import com.tangl.pan.server.modules.user.constants.UserConstants;
import com.tangl.pan.server.modules.user.context.*;
import com.tangl.pan.server.modules.user.converter.UserConverter;
import com.tangl.pan.server.modules.user.entity.TPanUser;
import com.tangl.pan.server.modules.user.service.IUserService;
import com.tangl.pan.server.modules.user.mapper.TPanUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;

/**
 * @author tangl
 * @description 用户业务层实现
 * @createDate 2023-07-23 23:38:02
 */
@Service(value = "userService")
public class UserServiceImpl extends ServiceImpl<TPanUserMapper, TPanUser> implements IUserService {

    @Autowired
    private UserConverter userConverter;

    @Autowired
    private IUserFileService userFileService;

    @Autowired
    private CacheManager cacheManager;

    /**
     * 用户注册的业务实现
     * 需要实现的功能点：
     * 1、注册用户信息
     * 2、创建新用户的根本目录信息
     * 需要实现的技术难点：
     * 1、该业务是幂等的
     * 2、保证用户名全局唯一
     * 实现技术难点的处理方案
     * 1、幂等性通过数据库对于用户名字段添加唯一索引，上层业务捕获对应的冲突异常，转换返回
     *
     * @param userRegisterContext context
     * @return 用户ID
     */
    @Override
    public Long register(UserRegisterContext userRegisterContext) {
        assembleUserEntity(userRegisterContext);
        doRegister(userRegisterContext);
        createUserRootFolder(userRegisterContext);
        return userRegisterContext.getEntity().getUserId();
    }

    /**
     * 用户登录业务实现
     * 1、用户的登录信息校验
     * 2、生成一个具有时效性的 accessToken
     * 3、将 accessToken 缓存起来, 去实现单机登录
     *
     * @param userLoginContext userLoginContext
     * @return accessToken
     */
    @Override
    public String login(UserLoginContext userLoginContext) {
        checkLoginInfo(userLoginContext);
        generateAndSaveAccessToken(userLoginContext);
        return userLoginContext.getAccessToken();
    }

    /**
     * 用户退出登录
     * 1、清楚用户的登录凭证缓存
     *
     * @param userId userId
     */
    @Override
    public void exit(Long userId) {
        try {
            Cache cache = cacheManager.getCache(CacheConstants.T_PAN_CACHE_NAME);
            assert cache != null;
            cache.evict(UserConstants.USER_LOGIN_PREFIX + userId);
        } catch (Exception e) {
            throw new TPanBusinessException("用户退出登录失败");
        }
    }

    /**
     * 校验用户名
     *
     * @param checkUsernameContext checkUsernameContext
     * @return question
     */
    @Override
    public String checkUsername(CheckUsernameContext checkUsernameContext) {
        String question = baseMapper.selectQuestionByUsername(checkUsernameContext.getUsername());
        if (StringUtils.isBlank(question)) {
            throw new TPanBusinessException("没有此用户");
        }
        return question;
    }

    /**
     * 校验密保答案
     *
     * @param checkAnswerContext checkAnswerContext
     * @return 校验结果
     */
    @Override
    public String checkAnswer(CheckAnswerContext checkAnswerContext) {
        QueryWrapper<TPanUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", checkAnswerContext.getUsername());
        queryWrapper.eq("question", checkAnswerContext.getQuestion());
        queryWrapper.eq("answer", checkAnswerContext.getAnswer());
        int count = count(queryWrapper);
        if (count == 0) {
            throw new TPanBusinessException("密保答案错误");
        }
        return generateCheckAnswerToken(checkAnswerContext);
    }

    /**
     * 重置用户密码
     * 1、校验 token 是否有效
     * 2、重置密码
     *
     * @param resetPasswordContext resetPasswordContext
     */
    @Override
    public void resetPassword(ResetPasswordContext resetPasswordContext) {
        checkForgetPasswordToken(resetPasswordContext);
        checkAndResetUserPassword(resetPasswordContext);
    }

    /**
     * 校验用户信息并重置用户密码
     *
     * @param resetPasswordContext resetPasswordContext
     */
    private void checkAndResetUserPassword(ResetPasswordContext resetPasswordContext) {
        String username = resetPasswordContext.getUsername();
        String password = resetPasswordContext.getPassword();
        TPanUser entity = getTPanUserByUsername(username);
        if (Objects.isNull(entity)) {
            throw new TPanBusinessException("用户信息不存在");
        }
        String newDbPassword = PasswordUtil.encryptPassword(entity.getSalt(), password);
        entity.setPassword(newDbPassword);
        entity.setUpdateTime(new Date());
        if (!updateById(entity)) {
            throw new TPanBusinessException("重置用户密码失败");
        }
    }

    /**
     * 验证忘记密码的 token 是否有效
     *
     * @param resetPasswordContext resetPasswordContext
     */
    private void checkForgetPasswordToken(ResetPasswordContext resetPasswordContext) {
        String token = resetPasswordContext.getToken();
        String username = resetPasswordContext.getUsername();
        Object value = JwtUtil.analyzeToken(token, UserConstants.FORGET_USERNAME);
        if (Objects.isNull(value)) {
            throw new TPanBusinessException(ResponseCode.TOKEN_EXPIRE);
        }
        String tokenUsername = String.valueOf(value);
        if (!Objects.equals(tokenUsername, username)) {
            throw new TPanBusinessException("token错误");
        }
    }

    /**
     * 生成用户忘记密码-校验密保答案通过的临时 token
     * token 失效的时间为 5 分钟之后
     *
     * @param checkAnswerContext checkAnswerContext
     * @return 临时 token
     */
    private String generateCheckAnswerToken(CheckAnswerContext checkAnswerContext) {
        return JwtUtil.generateToken(checkAnswerContext.getUsername(), UserConstants.FORGET_USERNAME, checkAnswerContext.getUsername(), UserConstants.FIVE_MINUTES_LONG);
    }

    /**
     * 生成并保存登录之后的凭证
     *
     * @param userLoginContext context
     */
    private void generateAndSaveAccessToken(UserLoginContext userLoginContext) {
        TPanUser entity = userLoginContext.getEntity();
        String accessToken = JwtUtil.generateToken(entity.getUsername(), UserConstants.LOGIN_USER_ID, entity.getUserId(), UserConstants.ONE_DAY_LONG);
        Cache cache = cacheManager.getCache(CacheConstants.T_PAN_CACHE_NAME);
        if (Objects.isNull(cache)) throw new TPanFrameworkException("获取缓存对象失败");
        cache.put(UserConstants.USER_LOGIN_PREFIX + entity.getUserId(), accessToken);
        userLoginContext.setAccessToken(accessToken);
    }

    /**
     * 校验用户名密码
     *
     * @param userLoginContext context
     */
    private void checkLoginInfo(UserLoginContext userLoginContext) {
        String username = userLoginContext.getUsername();
        String password = userLoginContext.getPassword();
        TPanUser entity = getTPanUserByUsername(username);
        if (Objects.isNull(entity)) {
            throw new TPanBusinessException("用户名称不存在");
        }
        String salt = entity.getSalt();
        String encPassword = PasswordUtil.encryptPassword(salt, password);
        String dbPassword = entity.getPassword();
        if (!Objects.equals(encPassword, dbPassword)) {
            throw new TPanBusinessException("密码信息不正确");
        }
        userLoginContext.setEntity(entity);
    }

    /**
     * 通过用户名获取用户实体信息
     *
     * @param username 用户名
     * @return entity
     */
    private TPanUser getTPanUserByUsername(String username) {
        QueryWrapper<TPanUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return getOne(queryWrapper);
    }

    /**
     * 创建用户的根目录信息
     *
     * @param userRegisterContext context
     */
    private void createUserRootFolder(UserRegisterContext userRegisterContext) {
        CreateFolderContext createFolderContext = new CreateFolderContext();
        createFolderContext.setParentId(FileConstants.TOP_PARENT_ID);
        createFolderContext.setFolderName(FileConstants.ALL_FILE_CN_STR);
        createFolderContext.setUserId(userRegisterContext.getEntity().getUserId());
        userFileService.createFolder(createFolderContext);
    }

    /**
     * 实现注册用户的业务
     * 需要捕获数据库的唯一索引冲突
     *
     * @param userRegisterContext context
     */
    private void doRegister(UserRegisterContext userRegisterContext) {
        TPanUser entity = userRegisterContext.getEntity();
        if (Objects.isNull(entity)) {
            throw new TPanBusinessException(ResponseCode.ERROR);
        }
        try {
            if (!save(entity)) {
                throw new TPanBusinessException("用户注册失败");
            }
        } catch (DuplicateKeyException e) {
            throw new TPanBusinessException("用户名已存在");
        }
    }

    /**
     * 实体转换
     * 由上下文信息转换成用户实体
     *
     * @param userRegisterContext context
     */
    private void assembleUserEntity(UserRegisterContext userRegisterContext) {
        TPanUser entity = userConverter.userRegisterContext2TPanUser(userRegisterContext);
        String salt = PasswordUtil.getSalt();
        String password = PasswordUtil.encryptPassword(salt, userRegisterContext.getPassword());
        entity.setUserId(IdUtil.get());
        entity.setSalt(salt);
        entity.setPassword(password);
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());
        userRegisterContext.setEntity(entity);
    }
}
