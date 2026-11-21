package com.xxl.tool.cache.impl;

import java.util.HashMap;

/**
 * 无限容量缓存
 *
 * 1、无容量限制，不会因为容量触发缓存清理
 * 2、线程安全，父类按 key 分桶加锁控制写入与过期删除；
 * 3、优劣势：
 *      - 优点：无容量限制，不会因为容量触发缓存清理；
 *      - 问题：内存占用不可控，容易造成内存溢出；需要自行控制写入量并设置有效期，避免内存无限增长；
 *
 * <p>注意：{@link #doPrune()} 当前为空实现，定时或手动调用 {@code prune()} 都不会移除过期数据；
 * 过期数据仅在 {@code get} / {@code containsKey} 读取时被惰性移除。</p>
 *
 * <p>生命周期：无需关闭；定时清理任务由 {@code CacheTool#stop()} 取消。</p>
 *
 * <p>入参：不修改 key/value 对象；{@code put(null, ...)} 被忽略。</p>
 *
 * @param <K> 键类型
 * @param <V> 值类型
 */
public class UnlimitedCache<K, V> extends ReentrantCache<K, V>{
    private static final long serialVersionUID = 42L;

    /**
     * 构造无容量限制缓存
     *
     * @param timeout    有效期，单位毫秒；{@code 0} 表示永不过期
     * @param expireType 过期策略，{@code true} 为 expireAfterWrite（按写入时间），
     *                   {@code false} 为 expireAfterAccess（按最后访问时间）
     */
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

    /**
     * 不执行清理：无容量限制，当前实现不会清理过期数据
     * <p>
     * 过期数据只在读取时惰性移除，因此 {@code size()} 可能包含较多过期条目。
     *
     * @return 固定返回 {@code 0}
     */
    @Override
    protected int doPrune() {
        return 0;
    }

}
