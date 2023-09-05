package com.tangl.pan.cache.caffeine.test.instance;

import com.tangl.pan.cache.core.constants.CacheConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/**
 * @author tangl
 * @description cache 注解测试实体
 * @create 2023-07-24 11:29
 */
@Component
@Slf4j
public class CacheAnnotationTester {
    /**
     * 测试自适应缓存注解
     *
     * @param name 参数
     */
    @Cacheable(cacheNames = CacheConstants.T_PAN_CACHE_NAME, key = "#name", sync = true)
    public void testCacheable(String name) {
        log.info("call com.tangl.pan.cache.caffeine.test.instance.CacheAnnotationTester.testCacheable, param is {}", name);
    }
}
