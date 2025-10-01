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
 */
public class FIFOCache<K, V> extends ReentrantCache<K, V> {
    private static final long serialVersionUID = 42L;

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