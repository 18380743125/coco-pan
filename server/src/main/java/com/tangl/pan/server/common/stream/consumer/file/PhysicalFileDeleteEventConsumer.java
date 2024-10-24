package com.tangl.pan.server.common.stream.consumer.file;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tangl.pan.core.constants.PanConstants;
import com.tangl.pan.server.common.stream.channel.PanChannels;
import com.tangl.pan.server.common.stream.event.file.PhysicalFileDeleteEvent;
import com.tangl.pan.server.common.stream.event.log.ErrorLogEvent;
import com.tangl.pan.server.modules.file.entity.TPanFile;
import com.tangl.pan.server.modules.file.entity.TPanUserFile;
import com.tangl.pan.server.modules.file.enums.FolderFlagEnum;
import com.tangl.pan.server.modules.file.service.IFileService;
import com.tangl.pan.server.modules.file.service.IUserFileService;
import com.tangl.pan.storage.engine.core.StorageEngine;
import com.tangl.pan.storage.engine.core.context.DeleteFileContext;
import com.tangl.pan.stream.core.AbstractConsumer;
import com.tangl.pan.stream.core.IStreamProducer;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class PhysicalFileDeleteEventConsumer extends AbstractConsumer {

    @Autowired
    private IFileService fileService;

    @Autowired
    private IUserFileService userFileService;

    @Autowired
    private StorageEngine storageEngine;

    @Autowired
    @Qualifier(value = "defaultStreamProducer")
    private IStreamProducer producer;

    /**
     * 监听物理文件删除事件的执行器
     * <p>
     * 该执行器是一个资源释放器，释放被物理删除的文件列表中关联的实体文件记录
     * 1、查询所有无引用的实体文件记录
     * 2、删除记录
     * 3、物理清理文件（委托存储引擎）
     *
     * @param message 物理文件删除的消息
     */
    @StreamListener(PanChannels.PHYSICAL_FILE_DELETE_INPUT)
    public void physicalFileDelete(Message<PhysicalFileDeleteEvent> message) {
        if (isEmptyMessage(message)) {
            return;
        }
        printLog(message);
        PhysicalFileDeleteEvent event = message.getPayload();
        List<TPanUserFile> allRecords = event.getAllRecords();
        if (CollectionUtils.isEmpty(allRecords)) {
            return;
        }
        List<Long> realFileIdList = findAllUnusedRealFileIdList(allRecords);

        if (CollectionUtils.isEmpty(realFileIdList)) {
            return;
        }

        List<TPanFile> realFileRecords = fileService.listByIds(realFileIdList);

        if (CollectionUtils.isEmpty(realFileRecords)) {
            return;
        }

        if (!fileService.removeByIds(realFileIdList)) {
            ErrorLogEvent errorLogEvent = new ErrorLogEvent("文件实体记录：" + JSON.toJSONString(realFileIdList) + "，物理删除失败，请执行手动删除", PanConstants.ZERO_LONG);
            producer.sendMessage(PanChannels.ERROR_LOG_OUTPUT, errorLogEvent);
            return;
        }
        try {
            physicalFileDeleteByStorageEngine(realFileRecords);
        } catch (IOException e) {
            ErrorLogEvent errorLogEvent = new ErrorLogEvent("物理文件：" + JSON.toJSONString(realFileIdList) + "，删除失败，请执行手动删除", PanConstants.ZERO_LONG);
            producer.sendMessage(PanChannels.ERROR_LOG_OUTPUT, errorLogEvent);
        }
    }

    /**
     * 物理清理文件（委托存储引擎）
     *
     * @param realFileRecords 真实文件记录
     */
    private void physicalFileDeleteByStorageEngine(List<TPanFile> realFileRecords) throws IOException {
        List<String> realPathList = realFileRecords.stream().map(TPanFile::getRealPath).collect(Collectors.toList());
        DeleteFileContext deleteFileContext = new DeleteFileContext();
        deleteFileContext.setRealFilePathList(realPathList);
        storageEngine.delete(deleteFileContext);
    }

    /**
     * 查找所有没有被引用的真实文件记录 ID 集合
     *
     * @param allRecords 所有文件记录集合
     * @return List<Long>
     */
    private List<Long> findAllUnusedRealFileIdList(List<TPanUserFile> allRecords) {
        return allRecords.stream()
                .filter(record -> Objects.equals(record.getFolderFlag(), FolderFlagEnum.NO.getCode()))
                .filter(this::isUnused)
                .map(TPanUserFile::getRealFileId).collect(Collectors.toList());
    }

    /**
     * 校验文件的真实文件 ID 是不是没有被引用了
     *
     * @param record 文件记录
     * @return boolean
     */
    private boolean isUnused(TPanUserFile record) {
        QueryWrapper<TPanUserFile> queryWrapper = Wrappers.query();
        queryWrapper.eq("real_file_id", record.getRealFileId());
        return userFileService.count(queryWrapper) == PanConstants.ZERO_INT;
    }
}
