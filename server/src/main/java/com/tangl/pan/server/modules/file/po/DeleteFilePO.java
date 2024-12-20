package com.tangl.pan.server.modules.file.po;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@ApiModel(value = "批量删除文件入参对象实体")
@Data
public class DeleteFilePO implements Serializable {
    private static final long serialVersionUID = 2700266833584983038L;

    @ApiModelProperty(value = "要删除的文件ID，多个使用通用的分隔符分隔", required = true)
    @NotBlank(message = "请选择要删除的文件")
    private String fileIds;
}
