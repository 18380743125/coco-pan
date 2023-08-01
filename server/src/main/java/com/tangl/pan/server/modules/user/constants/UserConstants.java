package com.tangl.pan.server.modules.user.constants;

/**
 * @author tangl
 * @description 用户模块的常量类
 * @create 2023-07-31 13:59
 */
public interface UserConstants {
    /**
     * 登录用户的用户 ID 的 key 值
     */
    String LOGIN_USER_ID = "LOGIN_USER_ID";

    /**
     * 用户登录缓存前缀
     */
    String USER_LOGIN_PREFIX = "USER_LOGIN_";

    /**
     * 用户忘记密码-重置密码临时 token 的 key
     */
    String FORGET_USERNAME = "FORGET_USERNAME";

    /**
     * 一天的 ms
     */
    Long ONE_DAY_LONG = 24L * 60L * 60L * 1000L;

    /**
     * 5分钟的 ms
     */
    Long FIVE_MINUTES_LONG = 5L * 60L * 1000L;
}
