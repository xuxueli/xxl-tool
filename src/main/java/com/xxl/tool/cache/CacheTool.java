package com.xxl.tool.cache;

import com.xxl.tool.cache.iface.Cache;
import com.xxl.tool.cache.iface.CacheListener;
import com.xxl.tool.cache.iface.CacheLoader;
import com.xxl.tool.cache.impl.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Cache Tool
 *
 * @param <K>	cache key type
 * @param <V>	cache value type
 * @author xuxueli 2018-01-22
 */
public class CacheTool<K, V> {
    private static final Logger logger = LoggerFactory.getLogger(CacheTool.class);

    // ---------------------- build ----------------------

    /**
     * new cache
     *
     * @return CacheTool
     */
    public static <K, V> CacheTool<K, V> newCache() {
        return newLRUCache();
    }

    /**
     * FIFO cache
     *
     * @return CacheTool
     */
    public static <K, V> CacheTool<K, V> newFIFOCache() {
        return newFIFOCache(1000);
    }

    /**
     * FIFO cache
     *
     * @param capacity	capacity of cache
     * @return CacheTool
     */
    public static <K, V> CacheTool<K, V> newFIFOCache(int capacity) {
        return new CacheTool<K, V>()
                .cache(CacheType.FIFO)
                .capacity(capacity);
    }

    /**
     * LRU cache
     *
     * @return CacheTool
     */
    public static <K, V> CacheTool<K, V> newLRUCache() {
        return newLRUCache(1000);
    }

    /**
     * LRU cache
     *
     * @param capacity	capacity of cache
     * @return CacheTool
     */
    public static <K, V> CacheTool<K, V> newLRUCache(int capacity) {
        return new CacheTool<K, V>()
                .cache(CacheType.LRU)
                .capacity(capacity);
    }

    /**
     * LFU cache
     *
     * @return CacheTool
     */
    public static <K, V> CacheTool<K, V> newLFUCache() {
        return newLFUCache(1000);
    }

    /**
     * LFU cache
     *
     * @param capacity	capacity of cache
     * @return CacheTool
     */
    public static <K, V> CacheTool<K, V> newLFUCache(int capacity) {
        return new CacheTool<K, V>()
                .cache(CacheType.LFU)
                .capacity(capacity);
    }

    /**
     * 无容量限制
     *
     * @return CacheTool
     */
    public static <K, V> CacheTool<K, V> newUnlimitedCache() {
        return newUnlimitedCache(0);
    }

    /**
     * 无容量限制
     *
     * @param timeout	timeout of cache, for millisecond
     * @return CacheTool
     */
    public static <K, V> CacheTool<K, V> newUnlimitedCache(int timeout) {
        return new CacheTool<K, V>()
                .cache(CacheType.UNLIMITED)
                .expireAfterWrite(timeout);
    }

    // ---------------------- param ----------------------

    /**
     * 缓存有效期，{@code 0} 表示无限制，单位毫秒
     */
    protected long timeout = 0;

    /**
     * 缓存过期方式：
     * 		- true: expireAfterWrite , 从缓存写入开始计算，有效期到期则被清除
     * 		- false: expireAfterAccess, 从缓存最后一次访问开始计算，有效期到期则被清除
     */
    protected boolean expireType = false;

    /**
     * 缓存容量, {@code 0} 表示无限制
     */
    protected int capacity = 0;

    /**
     * 缓存类型
     */
    protected CacheType cacheType;

    /**
     * 缓存对象创建组件
     */
    protected CacheLoader<K, V> loader;

    /**
     * 缓存变更（删除）监听器
     */
    protected CacheListener<K, V> listener;

    /**
     * 定时清理延时，{@code 0} 表示不启用，单位：毫秒
     */
    protected long pruneInterval = 0;

    /**
     * 定时清理任务
     */
    protected ScheduledFuture<?> pruneJobFuture;

    /**
     * 缓存实现实例
     */
    protected Cache<K, V> cache;

    /**
     * 设置 过期时长
     */
    public CacheTool<K, V> expireAfterWrite(long timeout) {
        this.timeout = timeout;
        this.expireType = true;
        return this;
    }

    /**
     * 设置 过期时长
     */
    public CacheTool<K, V> expireAfterAccess(long timeout) {
        this.timeout = timeout;
        this.expireType = false;
        return this;
    }

    /**
     * 设置 容量
     */
    public CacheTool<K, V> capacity(int capacity) {
        this.capacity = capacity;
        return this;
    }

    /**
     * 设置 缓存类型
     */
    public CacheTool<K, V> cache(CacheType cacheType) {
        this.cacheType = cacheType;
        return this;
    }

    /**
     * 设置 缓存对象创建组件
     */
    public CacheTool<K, V> loader(CacheLoader<K, V> cacheLoader) {
        this.loader = cacheLoader;
        return this;
    }

    /**
     * 设置 缓存变更（删除）监听器
     */
    public CacheTool<K, V> listener(CacheListener<K, V> listener) {
        this.listener = listener;
        return this;
    }

    /**
     * 设置 定时清理延时, 单位：毫秒
     */
    public CacheTool<K, V> pruneInterval(long pruneInterval) {
        this.pruneInterval = pruneInterval;
        return this;
    }

    /**
     * build cache
     */
    public <K, V> Cache<K, V> build() {

        // build cache
        if (cacheType == CacheType.NONE) {
            cache = new NoCache<>();
        } else if (cacheType == CacheType.FIFO) {
            cache = new FIFOCache<>(capacity, timeout, expireType);
        } else if (cacheType == CacheType.LFU) {
            cache = new LFUCache<>(capacity, timeout, expireType);
        } else if (cacheType == CacheType.LRU) {
            cache = new LRUCache<>(capacity, timeout, expireType);
        } else if (cacheType == CacheType.UNLIMITED) {
            cache = new UnlimitedCache<>(timeout, expireType);
            // 由于无容量限制，主动开启 “定时清理”
            if (pruneInterval <=0) {
                pruneInterval = 5 * 1000L;
            }
        } else {
            // default lfu
            throw new RuntimeException("cacheType invalid.");
        }

        // set listener
        if (listener != null) {
            cache.setListener(listener);
        }
        // set loader
        if (loader != null) {
            cache.setLoader(loader);
        }

        // prune cycle
        if (pruneInterval > 0) {
            pruneJobFuture = schedule(() -> {
                try {
                    cache.prune();
                } catch (Exception e) {
                    logger.error("prune cache error", e);
                }
            }, pruneInterval);
        }

        return (Cache<K, V>) cache;
    }

    /**
     * stop
     */
    public void stop() {
        if (pruneJobFuture != null) {
            pruneJobFuture.cancel(true);
        }
    }


    // ---------------------- schedule prune ----------------------

    /**
     * schedule prune
     */
    private static final ScheduledExecutorService pruneTimer;

    static {
        pruneTimer = new ScheduledThreadPoolExecutor(1);
        // JVM销毁时，终止 pruneTimer
        Runtime.getRuntime().addShutdownHook(new Thread(pruneTimer::shutdownNow));
    }

    /**
     * schedule task
     */
    private ScheduledFuture<?> schedule(Runnable task, long delay) {
        return pruneTimer.scheduleAtFixedRate(task, delay, delay, TimeUnit.MILLISECONDS);
    }

}