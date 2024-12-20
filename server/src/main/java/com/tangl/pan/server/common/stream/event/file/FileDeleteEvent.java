package com.tangl.pan.server.common.stream.event.file;

import lombok.*;

import java.io.Serializable;
import java.util.List;

/**
 * 文件删除事件
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@ToString
@NoArgsConstructor
public class FileDeleteEvent implements Serializable {

    private static final long serialVersionUID = -1922543273278238212L;

    private List<Long> fileIdList;

    public FileDeleteEvent(List<Long> fileIdList) {
        this.fileIdList = fileIdList;
    }
}
