package com.xxl.tool.cache.impl;

import com.xxl.tool.cache.iface.Cache;
import com.xxl.tool.cache.iface.CacheListener;
import com.xxl.tool.cache.iface.CacheLoader;
import com.xxl.tool.cache.model.CacheKey;
import com.xxl.tool.cache.model.CacheObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Reentrant Cache
 *
 * @param <K> 键类型
 * @param <V> 值类型
 */
public abstract class ReentrantCache<K, V> implements Cache<K, V> {
	private static final Logger logger = LoggerFactory.getLogger(ReentrantCache.class);
    private static final long serialVersionUID = 42L;

    // ---------------------- field ----------------------

    /**
     * 缓存有效期，{@code 0} 表示无限制，单位毫秒
     */
    protected long timeout;

    /**
     * 缓存容量, {@code 0} 表示无限制
     */
    protected int capacity;

    /**
     * 缓存过期方式：
     *      - true: expireAfterWrite , 从缓存写入开始计算，有效期到期则被清除
     *      - false: expireAfterAccess, 从缓存最后一次访问开始计算，有效期到期则被清除
     */
    protected boolean expireType;

    /**
     * write key map
     */
    protected final ConcurrentMap<Long, Lock> writeLockMap = new ConcurrentHashMap<>();

    /**
     * write lock count
     */
    protected final int writeLockCount = 100;

    /**
     * 缓存数据
     */
    protected volatile Map<CacheKey<K>, CacheObject<K, V>> cacheMap;

    /**
     * 缓存命中计数
     */
    protected LongAdder hitCount = new LongAdder();
    /**
     * 缓存未命中计数
     */
    protected LongAdder missCount = new LongAdder();

    /**
     * 缓存监听
     */
    protected CacheListener<K, V> listener;

    /**
     * 缓存加载器
     */
    protected CacheLoader<K, V> loader;

    // ---------------------- lock ----------------------

    /**
     * 获取key的锁
     */
    private Lock getKeyLock(K key) {
        long keyHash = key != null ? key.hashCode() % writeLockCount : -1;
        return writeLockMap.computeIfAbsent(keyHash, (Long k) -> new ReentrantLock());
    }

    // ---------------------- put ----------------------

    @Override
    public void put(K key, V object) {
        // valid
        if (key == null) {
            return;
        }

        // put
        Lock writeLock = getKeyLock(key);
        writeLock.lock();
        try {
            CacheKey<K> cacheKey = CacheKey.of(key);
            CacheObject<K, V> cacheObject = new CacheObject<>(key, object, timeout, expireType);

            // do put
            if (cacheMap.containsKey(cacheKey)) {
                cacheMap.put(cacheKey, cacheObject);
            } else {
                if (isFull()) {
                    doPrune();
                }
                cacheMap.put(cacheKey, cacheObject);
            }
        } finally {
            writeLock.unlock();
        }
    }

    // ---------------------- get ----------------------


    @Override
    public V get(K key) {
        return get(key, this.loader);
    }

    @Override
    public V getIfPresent(K key) {
        return get(key, null);
    }

    @Override
    public V get(K key, CacheLoader<K, V> cacheLoader) {
        // get
        V v = getOrRemoveExpired(key, true, true);

        // cacheLoader
        if (null == v && null != cacheLoader) {
            Lock writeLock = getKeyLock(key);
            writeLock.lock();
            try {
                // valid 2
                v = getOrRemoveExpired(key, true, false);
                if (null == v) {
                    // do cacheLoader
                    v = cacheLoader.load(key);
                    put(key, v);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                writeLock.unlock();
            }
        }
        return v;
    }

    /**
     * 获取缓存对象
     * <p>
     * 1、缓存对象不存在 或 已过期：返回 {@code null}
     * 2、每次调用此方法，可选是否 刷新最后访问时间，即重新计算超时时间
     * 3、每次调用此方法，可选是否 更新计数器
     *
     * @param key                键
     * @param isUpdateLastAccess 是否更新最后访问时间
     * @param isUpdateCount      是否更新计数器
     * @return 值对象：
     */
    private V getOrRemoveExpired(final K key, final boolean isUpdateLastAccess, final boolean isUpdateCount) {
        CacheObject<K, V> cacheObject;

        // lock
        Lock writeLock = getKeyLock(key);
        writeLock.lock();
        try {
            // get cache
            cacheObject = this.cacheMap.get(CacheKey.of(key));
            // remove expired cache
            if (null != cacheObject && cacheObject.isExpired()) {

                // do remove
                removeWithoutLock(key);

                // remove listener
                onRemove(cacheObject);

                // mark null
                cacheObject = null;
            }
        } finally {
            writeLock.unlock();
        }

        // count
        if (isUpdateCount) {
            if (null == cacheObject) {
                missCount.increment();
            } else {
                hitCount.increment();
            }
        }

        // return
        if (cacheObject == null) {
            return null;
        }
        return cacheObject.get(isUpdateLastAccess);
    }

    // ---------------------- query ----------------------

    @Override
    public boolean containsKey(K key) {
        return null != getOrRemoveExpired(key, false, false);
    }

    @Override
    public Map<K, V> asMap() {
        return this.cacheMap
                .values()
                .stream()
                .filter(cacheObject -> !cacheObject.isExpired())
                .collect(Collectors.toMap(CacheObject::getKey, CacheObject::getValue));
    }

    @Override
    public int size() {
        return cacheMap.size();
    }

    @Override
    public boolean isFull() {
        return (capacity > 0) && (cacheMap.size() >= capacity);
    }

    @Override
    public boolean isEmpty() {
        return cacheMap.isEmpty();
    }

    // ---------------------- delete ----------------------

    @Override
    public void remove(K key) {
        CacheObject<K, V> cacheObject;

        // remove
        Lock writeLock = getKeyLock(key);
        writeLock.lock();
        try {
            cacheObject = removeWithoutLock(key);
        } finally {
            writeLock.unlock();
        }

        // remove - listener
        onRemove(cacheObject);
    }

    /**
     * 移除key对应的对象，不加锁
     *
     * @param key 键
     * @return 移除的对象，无返回null
     */
    protected CacheObject<K, V> removeWithoutLock(K key) {
        return cacheMap.remove(CacheKey.of(key));
    }

    /**
     * 对象移除回调
     *
     * @param cacheObject 被缓存的对象
     */
    protected void onRemove(CacheObject<K, V> cacheObject) {
        if (this.listener != null && cacheObject != null) {
            try {
                this.listener.onRemove(cacheObject.getKey(), cacheObject.getValue());
            } catch (Exception e) {
                logger.error("cache listener onRemove error, cacheObject:{}", cacheObject, e);
            }
        }
    }

    @Override
    public final int prune() {
        Lock writeLock = getKeyLock(null);
        writeLock.lock();
        try {
            return doPrune();
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 清理实现<br>
     * 子类实现此方法时无需加锁
     *
     * @return 清理数据量
     */
    protected abstract int doPrune();

    @Override
    public void clear() {
        Lock writeLock = getKeyLock(null);
        writeLock.lock();
        try {
            cacheMap.clear();
        } finally {
            writeLock.unlock();
        }
    }

    // ---------------------- monitor ----------------------

    /**
     * @return 命中数
     */
    public long hitCount() {
        return hitCount.sum();
    }

    /**
     * @return 丢失数
     */
    public long missCount() {
        return missCount.sum();
    }

    // ---------------------- info ----------------------

    @Override
    public int capacity() {
        return capacity;
    }

    @Override
    public long timeout() {
        return timeout;
    }

    // ---------------------- listener ----------------------

    /**
     * 设置监听
     */
    @Override
    public ReentrantCache<K, V> setListener(CacheListener<K, V> listener) {
        this.listener = listener;
        return this;
    }

    @Override
    public ReentrantCache<K, V> setLoader(CacheLoader<K, V> loader) {
        this.loader = loader;
        return this;
    }

    // ------------------------ other ------------------------

    /**
     * 是否支持过期对象清理，仅设置 过期时间 才支持；
     */
    /*public boolean isPruneExpiredActive() {
        return (timeout > 0);
    }*/

    @Override
    public String toString() {
        Lock writeLock = getKeyLock(null);
        writeLock.lock();
        try {
            return this.cacheMap.toString();
        } finally {
            writeLock.unlock();
        }
    }

}