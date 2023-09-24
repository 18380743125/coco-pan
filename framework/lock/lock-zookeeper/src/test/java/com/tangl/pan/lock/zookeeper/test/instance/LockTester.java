package com.tangl.pan.lock.zookeeper.test.instance;

import com.tangl.pan.lock.core.annotation.Lock;
import org.springframework.stereotype.Component;

/**
 * lock实测实例
 */
@Component
public class LockTester {

    @Lock(name = "test", keys = "#name", expireSecond = 10L)
    public void testLock(String name) {
        System.out.println(Thread.currentThread().getName() + " get the lock.");
        String result = "hello " + name;
        System.out.println(Thread.currentThread().getName() + " release the lock.");
    }

}
