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
        TokenBucket smoothBursty = TokenBucket.create(5.0);
        for (int i = 0; i < 10; i++) {
            double cost = smoothBursty.acquire();
            logger.info("{}: cost {}", DateTool.formatDateTime(new Date()), cost);
        }

    }

    @Test
    public void test2() {
        TokenBucket smoothWarmingUp = TokenBucket.create(5.0, 2, TimeUnit.SECONDS);
        for (int i = 0; i < 10; i++) {
            double cost = smoothWarmingUp.acquire();
            logger.info("{}: cost {}", DateTool.formatDateTime(new Date()), cost);
        }

    }

}
