package com.tangl.pan.server.modules.file.context;

import lombok.Data;

import java.io.Serializable;

/**
 * 创建文件夹上下文实体
 */
@Data
public class CreateFolderContext implements Serializable {

    private static final long serialVersionUID = -6583478622505132608L;

    /**
     * 父文件夹ID
     */
    private Long parentId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 文件夹名称
     */
    private String folderName;
}
