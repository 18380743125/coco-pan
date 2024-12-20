package com.tangl.pan.server.modules.file.context;

import lombok.Data;

import java.io.Serializable;

/**
 * 查询文件夹树的上下文实体
 */
@Data
public class QueryFolderTreeContext implements Serializable {

    private static final long serialVersionUID = 621768617879436800L;

    /**
     * 当前登录的用户ID
     */
    private Long userId;
}
