package com.tangl.pan.server.modules.user.context;

import com.tangl.pan.server.modules.user.entity.PanUser;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册业务的上下文实体对象
 */
@Data
public class UserRegisterContext implements Serializable {

    private static final long serialVersionUID = -2343901454309052651L;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 密保问题
     */
    private String question;

    /**
     * 密保答案
     */
    private String answer;

    /**
     * 用户实体对象
     */
    private PanUser entity;
}
