package com.xxl.tool.test.datastructure;

import com.xxl.tool.datastructure.BloomFilter;
import com.xxl.tool.datastructure.bloomfilter.Funnels;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BloomFilterTest {
    private static final Logger logger = LoggerFactory.getLogger(BloomFilterTest.class);

    @Test
    public void test1() {
        // 1. 容量
        int size = 1000000;
        // 2. 误判率
        double fpp = 0.01;
        BloomFilter<String> bloomFilter = BloomFilter.create(size, fpp);

        bloomFilter.put("item1");
        logger.info("mightContain: " + bloomFilter.mightContain("item1"));
        logger.info("mightContain: " + bloomFilter.mightContain("item2"));
    }

    @Test
    public void test2() {
        // 1. 容量
        int size = 1000000;
        // 2. 误判率
        double fpp = 0.01;
        BloomFilter<Long> bloomFilter = BloomFilter.create(Funnels.LONG, size, fpp);

        bloomFilter.put(999L);
        logger.info("mightContain: " + bloomFilter.mightContain(999L));
        logger.info("mightContain: " + bloomFilter.mightContain(666L));
    }

    @Test
    public void test3() {
        // 1. 容量
        int size = 10000;
        // 2. 误判率
        double fpp = 0.01;
        BloomFilter<String> bloomFilter = BloomFilter.create(size, fpp);

        // 测试大数据集
        int itemCount = 10000;
        for (int i = 0; i < itemCount; i++) {
            bloomFilter.put("item" + i);
        }

        // 检查一部分元素
        for (int i = 0; i < 100; i += 10) {
            Assertions.assertTrue(bloomFilter.mightContain("item" + i));
        }
    }

}
