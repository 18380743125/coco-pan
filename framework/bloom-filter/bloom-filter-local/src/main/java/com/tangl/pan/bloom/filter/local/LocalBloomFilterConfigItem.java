package com.tangl.pan.bloom.filter.local;

import lombok.Data;

/**
 * @author tangl
 * @description 本地布隆过滤器单体配置类
 * @create 2023-09-23 21:20
 */
@Data
public class LocalBloomFilterConfigItem {

    /**
     * 布隆过滤器的名称
     */
    private String name;

    /**
     * 数据通道的名称
     */
    private String funnelTypeName = FunnelType.LONG.name();

    /**
     * 数组的长度
     */
    private Long expectedInsertions = 1000000L;

    /**
     * 误判率
     */
    private Double fpp = 0.01D;

}
