package com.xxl.tool.cache.impl;

import com.xxl.tool.cache.model.CacheKey;
import com.xxl.tool.cache.model.CacheObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * LRU (least recently used)最近最久未使用缓存
 *
 * 1、元素持续添加直到存满，触发 清理全部过期对象；
 * 2、清理后缓存仍存满时，最久未被使用的对象将被移除；
 * （此缓存基于LinkedHashMap，因此当被缓存的对象每被访问一次，这个对象的key就到链表头部）
 * 3、优劣势：
 *      - 优势：简单、速度快；经常使用的对象，不会被被移除缓存；
 *      - 劣势：不能很快的访问缓存中不常用的对象；
 */
public class LRUCache<K, V> extends ReentrantCache<K, V> {
    private static final Logger logger = LoggerFactory.getLogger(LRUCache.class);
    private static final long serialVersionUID = 42L;

    public LRUCache(int capacity, long timeout, boolean expireType) {
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
         * 链表按照访问顺序排序，调用get方法后，相关元素会移至头部
         *
         * 1、capacity + 1 (初始容量)：预先分配足够的空间，减少后续扩容操作；
         * 2、1.0f (负载因子)：设置负载因子，1.0f 表示当哈希表完全填满时才会扩容；（存在主动清理，理论上永远不会填满，避免扩容）
         * 3、false (访问顺序)：false：按插入顺序排序；true：按访问顺序排序。
         */
        int finalCapacity = capacity;
        cacheMap = new LinkedHashMap<>(finalCapacity + 1, 1.0f, true) {
            @Override
            public boolean removeEldestEntry(Map.Entry<CacheKey<K>, CacheObject<K, V>> eldest) {
                // 当链表元素大于容量时，移除最老（最久未被使用）的元素
                if (size() > finalCapacity) {
                    if (null != listener) {
                        // remove-listener
                        try {
                            listener.onRemove(eldest.getValue().getKey(), eldest.getValue().getValue());
                        } catch (Exception e) {
                            logger.error("cache listener onRemove error, cacheObject:{}", eldest, e);
                        }
                    }
                    return true;
                }
                return false;
            }
        };

    }

    // ---------------------------------------------------------------- prune

    /**
     * 只 清理全部过期对象，LRU的实现会交给{@code LinkedHashMap}
     */
    @Override
    protected int doPrune() {
        /*if (!isPruneExpiredActive()) {
            return 0;
        }*/

        // doPrune
        int count = 0;
        Iterator<CacheObject<K, V>> values = super.cacheMap.values().iterator();

        // 清理全部过期对象
        while (values.hasNext()) {
            CacheObject<K, V> cacheObject = values.next();
            if (cacheObject.isExpired()) {
                // do remove
                values.remove();
                // remove-listener
                onRemove(cacheObject);
                count++;
            }
        }
        return count;
    }
}