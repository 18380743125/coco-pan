package com.tangl.pan.server.modules.file.context;

import lombok.Data;

import java.io.Serializable;

/**
 * 查询用户已上传的文件分片列表的上下文实体
 */
@Data
public class QueryUploadedChunksContext implements Serializable {

    private static final long serialVersionUID = -7843990733868419385L;

    /**
     * 文件的唯一标识
     */
    private String identifier;

    /**
     * 当前的登录用户ID
     */
    private Long userId;
}
