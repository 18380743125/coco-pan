package com.tangl.pan.server.modules.file.mapper;

import com.tangl.pan.server.modules.file.context.FileSearchContext;
import com.tangl.pan.server.modules.file.context.QueryFileListContext;
import com.tangl.pan.server.modules.file.entity.TPanUserFile;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tangl.pan.server.modules.file.vo.FileSearchResultVO;
import com.tangl.pan.server.modules.file.vo.UserFileVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * pan_user_file (用户文件信息表) 的数据库操作 Mapper
 */
public interface PanUserFileMapper extends BaseMapper<TPanUserFile> {

    /**
     * 查询用户的文件列表
     *
     * @param context 查询文件列表的上下文实体
     * @return List<UserFileVO>
     */
    List<UserFileVO> selectFileList(@Param("param") QueryFileListContext context);

    /**
     * 文件搜索
     *
     * @param context 搜索文件的上下文实体
     * @return List<FileSearchResultVO>
     */
    List<FileSearchResultVO> searchFile(@Param("param") FileSearchContext context);
}
