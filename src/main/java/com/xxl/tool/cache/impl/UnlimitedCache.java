package com.xxl.tool.cache.impl;

import java.util.HashMap;

/**
 * 无限容量缓存
 *
 * 1、无容量限制，不会因为容量触发缓存清理
 * 2、线程安全，父类存在读写锁控制；
 * 3、优劣势：
 *      - 优点：无容量限制，不会因为容量触发缓存清理；
 *      - 问题：内存占用不可用，容易造成内存溢出；需要结合 定义缓存清理策略 一起使用；
 *
 * @param <K>
 * @param <V>
 */
public class UnlimitedCache<K, V> extends ReentrantCache<K, V>{
    private static final long serialVersionUID = 42L;

    public UnlimitedCache(long timeout, boolean expireType) {
        // init
        this.capacity = 0;
        this.timeout = timeout;
        this.expireType = expireType;

        /**
         * 1、无容量限制，不会因为容量触发缓存清理
         * 2、线程安全，父类存在读写锁控制；
         */
        this.cacheMap = new HashMap<>();
    }

    @Override
    protected int doPrune() {
        return 0;
    }

}
