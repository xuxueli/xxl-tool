package com.xxl.tool.cache.impl;

import com.xxl.tool.cache.model.CacheObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

/**
 * LFU(least frequently used) 最少使用率缓存
 *
 * 1、元素持续添加直到存满，触发 清理全部过期对象；
 * 2、清理后缓存仍存满时，清除最少访问（访问计数最小）的对象；并将其他对象的访问数减去这个最小访问数，以便新对象进入后可以公平计数。
 * 3、优劣势：
 *      - 优势，热点数据可以常驻内存；
 *      - 劣势，复杂度较高；
 */
public class LFUCache<K, V> extends ReentrantCache<K, V> {
	private static final Logger logger = LoggerFactory.getLogger(LFUCache.class);
    private static final long serialVersionUID = 42L;

    public LFUCache(int capacity, long timeout, boolean expireType) {
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
         */
        cacheMap = new ConcurrentHashMap<>(capacity + 1, 1.0f);
    }

    // ---------------------------------------------------------------- prune

    /**
     * 1、清理全部过期对象
     * 2、清理后缓存仍存满时，清除最少访问（访问计数最小）的对象；并将其他对象的访问数减去这个最小访问数，以便新对象进入后可以公平计数。
     */
    @Override
    protected int doPrune() {

        // doPrune
        int count = 0;
        CacheObject<K, V> leastVisitedCacheObject = null;

        // 清理全部过期对象 + 找出访问最少的对象
        Iterator<CacheObject<K, V>> values = super.cacheMap.values().iterator();
        while (values.hasNext()) {
            CacheObject<K, V> cacheObject = values.next();
            if (cacheObject.isExpired()) {
                // do remove
                values.remove();
                // remove-listener
                onRemove(cacheObject);
                count++;
                continue;
            }

            // 找出访问最少的对象
            if (leastVisitedCacheObject == null || cacheObject.getAccessCount().get() < leastVisitedCacheObject.getAccessCount().get()) {
                leastVisitedCacheObject = cacheObject;
            }
        }

        // 清理结束后依旧是满的，则删除 访问最少的对象
        // 将其他对象的访问数减去这个最小访问数，以便新对象进入后可以公平计数。
        if (isFull() && leastVisitedCacheObject != null) {
            // do remove
            removeWithoutLock(leastVisitedCacheObject.getKey());
            // listener
            onRemove(leastVisitedCacheObject);
            count++;

            // 将其他对象的访问数减去这个最小访问数，以便新对象进入后可以公平计数。
            long minAccessCount = leastVisitedCacheObject.getAccessCount().get();
            values = super.cacheMap.values().iterator();
            while (values.hasNext()) {
                CacheObject<K, V> cacheObject = values.next();
                cacheObject.getAccessCount().addAndGet(-minAccessCount);
            }
        }

        return count;
    }
}