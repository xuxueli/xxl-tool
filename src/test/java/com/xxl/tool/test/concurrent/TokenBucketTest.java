package com.xxl.tool.test.concurrent;

import com.xxl.tool.concurrent.TokenBucket;
import com.xxl.tool.core.DateTool;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TokenBucketTest {
    private static Logger logger = LoggerFactory.getLogger(TokenBucketTest.class);

    @Test
    public void test() {
        // 创建平滑的令牌桶
        TokenBucket smoothBursty = TokenBucket.create(5.0);
        for (int i = 0; i < 10; i++) {
            // 获取令牌, 返回获取的令牌耗时
            double cost = smoothBursty.acquire();
            logger.info("{}: cost {}", DateTool.formatDateTime(new Date()), cost);
        }

    }

    @Test
    public void test2() {
        // 创建平滑的令牌桶
        TokenBucket smoothBursty = TokenBucket.create(5.0);
        for (int i = 0; i < 10; i++) {
            // 获取令牌, 尝试获取令牌, 100毫秒内返回结果
            boolean result = smoothBursty.tryAcquire(100, TimeUnit.MILLISECONDS);
            logger.info("{}: result {}", DateTool.formatDateTime(new Date()), result);
        }
    }

    @Test
    public void test3() {
        // 创建平滑的令牌桶，预热方式
        TokenBucket smoothWarmingUp = TokenBucket.create(5.0, 2, TimeUnit.SECONDS);
        for (int i = 0; i < 10; i++) {
            // 获取令牌, 返回获取的令牌耗时
            double cost = smoothWarmingUp.acquire();
            logger.info("{}: cost {}", DateTool.formatDateTime(new Date()), cost);
        }

    }

}
