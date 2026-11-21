package com.xxl.tool.cache.impl;

import com.xxl.tool.cache.model.CacheObject;

import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * FIFO(first in first out) 先进先出缓存
 *
 * 1、元素持续添加直到存满，触发 清理全部过期对象；
 * 2、清理后依然存满，则删除最早加入的缓存对象；
 * 3、优劣势：
 *      - 优势：实现简单；
 *      - 劣势，热点数据无法保障常驻内存；
 *
 * <p>线程安全：继承 {@link ReentrantCache}，按 key 分桶加锁，实例线程安全；
 * {@code asMap}/{@code size} 等查询为弱一致快照。</p>
 *
 * <p>生命周期：无需关闭；使用定时清理时由 {@code CacheTool#stop()} 取消任务。</p>
 *
 * <p>入参：不修改 key/value 对象；{@code put(null, ...)} 被忽略。</p>
 */
public class FIFOCache<K, V> extends ReentrantCache<K, V> {
    private static final long serialVersionUID = 42L;

    /**
     * 构造 FIFO 缓存
     *
     * @param capacity   容量上限，必须大于 0，否则抛 {@link IllegalArgumentException}
     * @param timeout    有效期，单位毫秒；{@code 0} 表示永不过期
     * @param expireType 过期策略，{@code true} 为 expireAfterWrite（按写入时间），
     *                   {@code false} 为 expireAfterAccess（按最后访问时间）
     */
    public FIFOCache(int capacity, long timeout, boolean expireType) {
        // valid
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must large than 0");
        }
        if (Integer.MAX_VALUE == capacity) {
            capacity -= 1;
        }

        // init
        this.capacity = capacity;
        this.timeout = timeout;
        this.expireType = expireType;

        /**
         * 1、capacity + 1 (初始容量)：预先分配足够的空间，减少后续扩容操作；
         * 2、1.0f (负载因子)：设置负载因子，1.0f 表示当哈希表完全填满时才会扩容；（存在主动清理，理论上永远不会填满，避免扩容）
         * 3、false (访问顺序)：false：按插入顺序排序；true：按访问顺序排序。
         */
        this.cacheMap = new LinkedHashMap<>(capacity + 1, 1.0f, false);
    }

    /**
     * 1、清理全部过期对象
     * 2、如清理后依然存满，则删除最早加入的缓存对象；
     * <p>
     * 调用方已持有锁；清理与淘汰均会触发监听器。
     *
     * @return 本次清理（过期移除 + 容量淘汰）的条目数量
     */
    @Override
    protected int doPrune() {

        // doPrune
        int count = 0;
        CacheObject<K, V> first = null;

        // 清理全部过期对象 + 找出链表头部元素（先入元素）
        final Iterator<CacheObject<K, V>> values = super.cacheMap.values().iterator();
        while (values.hasNext()) {
            CacheObject<K, V> cacheObject = values.next();
            if (cacheObject.isExpired()) {
                // do remove
                values.remove();
                // listener
                onRemove(cacheObject);
                count++;
                continue;
            }
            if (first == null) {
                first = cacheObject;
            }
        }

        // 清理结束后依旧是满的，则删除头部元素
        if (isFull() && null != first) {
            // do remove
            removeWithoutLock(first.getKey());
            // listener
            onRemove(first);
            count++;
        }
        return count;
    }

}