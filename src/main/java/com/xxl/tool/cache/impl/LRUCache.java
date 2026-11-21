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
 * （基于访问顺序的 LinkedHashMap，每次访问会将对象移动到链表尾部，链表头部即最久未使用对象）
 * 3、优劣势：
 *      - 优势：简单、速度快；经常使用的对象，不会被移除缓存；
 *      - 劣势：不能很快的访问缓存中不常用的对象；
 *
 * <p>线程安全：继承 {@link ReentrantCache}，写入与删除按 key 分桶加锁；
 * 但容量淘汰发生在 {@code LinkedHashMap#put} 内部，由当前写入线程触发。
 * 实例线程安全，{@code asMap}/{@code size} 等查询为弱一致快照。</p>
 *
 * <p>生命周期：无需关闭；使用定时清理时由 {@code CacheTool#stop()} 取消任务。</p>
 *
 * <p>入参：不修改 key/value 对象；{@code put(null, ...)} 被忽略。</p>
 */
public class LRUCache<K, V> extends ReentrantCache<K, V> {
    private static final Logger logger = LoggerFactory.getLogger(LRUCache.class);
    private static final long serialVersionUID = 42L;

    /**
     * 构造 LRU 缓存
     *
     * @param capacity   容量上限，必须大于 0，否则抛 {@link IllegalArgumentException}
     * @param timeout    有效期，单位毫秒；{@code 0} 表示永不过期
     * @param expireType 过期策略，{@code true} 为 expireAfterWrite（按写入时间），
     *                   {@code false} 为 expireAfterAccess（按最后访问时间）
     */
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
         * 链表按照访问顺序排序，调用get方法后，相关元素会移动至链表尾部（最近访问端）
         *
         * 1、capacity + 1 (初始容量)：预先分配足够的空间，减少后续扩容操作；
         * 2、1.0f (负载因子)：设置负载因子，1.0f 表示当哈希表完全填满时才会扩容；（存在主动清理，理论上永远不会填满，避免扩容）
         * 3、true (访问顺序)：true：按访问顺序排序；false：按插入顺序排序。
         */
        int finalCapacity = capacity;
        cacheMap = new LinkedHashMap<>(finalCapacity + 1, 1.0f, true) {
            /**
             * 当链表元素大于容量时，回调监听器并移除最老（最久未被使用）的元素
             *
             * @param eldest 链表头部（最久未使用）的条目
             * @return 需要移除时返回 {@code true}，否则返回 {@code false}
             */
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
     * 只 清理全部过期对象，LRU的容量淘汰会交给{@code LinkedHashMap}的 removeEldestEntry
     * <p>
     * 调用方已持有锁；过期移除会触发监听器。
     *
     * @return 本次清理的过期条目数量
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