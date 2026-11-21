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
 * 可重入缓存抽象基类：提供存储容器、按 key 分桶加锁、过期判断与命中统计等公共能力
 *
 * <p>线程安全：写操作按 key 的 {@code hashCode()} 对固定数量（100）的
 * {@link ReentrantLock} 分桶加锁，不同 key 可能共享同一把锁，并发写入与过期删除不会破坏内部结构；
 * {@code prune}、{@code clear}、{@code toString}
 * 使用 null key 对应的桶锁，与普通 key 的锁相互独立。{@code asMap}、{@code size}、{@code isEmpty}、
 * {@code isFull} 直接读取底层 Map 且不加锁，属于弱一致快照，遍历期间若有并发写入可能抛
 * {@link java.util.ConcurrentModificationException}，不保证与其他写操作原子。</p>
 *
 * <p>过期语义：{@code timeout} 单位为毫秒，{@code 0} 表示永不过期；{@code expireType = true} 为
 * expireAfterWrite（从写入时间起算），{@code false} 为 expireAfterAccess（从最后访问时间起算）。
 * 过期数据惰性删除：读取或 {@code containsKey} 发现过期时移除并回调监听器。</p>
 *
 * <p>生命周期：实例无需关闭；若通过 {@code CacheTool#pruneInterval(long)} 注册了定时清理，
 * 需调用 {@code CacheTool#stop()} 取消任务。</p>
 *
 * <p>入参：不会修改传入的 key/value 对象；key 为 {@code null} 时 {@code put} 忽略，读取按未命中处理。</p>
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
     * write key map：按 key 哈希分桶的锁表，桶数量固定为 {@link #writeLockCount}
     */
    protected final ConcurrentMap<Long, Lock> writeLockMap = new ConcurrentHashMap<>();

    /**
     * write lock count：锁分桶数量，key 按 {@code hashCode() % 100} 映射到对应锁
     */
    protected final int writeLockCount = 100;

    /**
     * 缓存数据：底层 Map 由子类创建，非线程安全，读写需由分桶锁保护
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

    /**
     * 写入缓存
     * <p>
     * key 已存在时直接覆盖；不存在且缓存已满时先执行 {@link #doPrune()}（清理过期数据并按策略淘汰），
     * 再写入新数据。key 为 {@code null} 时直接忽略。
     *
     * @param key    键，可为 {@code null}（忽略）
     * @param object 值对象，可为 {@code null}
     */
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


    /**
     * 读取缓存，未命中时使用 {@link #loader} 加载
     *
     * @param key 键，可为 {@code null}
     * @return 缓存值；未命中且无 loader 时返回 {@code null}
     */
    @Override
    public V get(K key) {
        return get(key, this.loader);
    }

    /**
     * 读取缓存，仅查询已有数据，不触发 loader
     *
     * @param key 键，可为 {@code null}
     * @return 缓存值；不存在或已过期返回 {@code null}
     */
    @Override
    public V getIfPresent(K key) {
        return get(key, null);
    }

    /**
     * 读取缓存，未命中时使用指定 loader 加载
     * <p>
     * 1、首次读取未命中会计入 missCount；命中则计入 hitCount；<br>
     * 2、需要加载时在对应 key 的锁内二次确认，避免同一 key 并发重复加载；<br>
     * 3、loader 抛出的异常包装为 {@link RuntimeException} 后抛出；<br>
     * 4、loader 返回 {@code null} 时仍会以 null 值写入，但后续读取按未命中处理。
     *
     * @param key         键，可为 {@code null}
     * @param cacheLoader 加载器，可为 {@code null}（为 null 时等价于 {@link #getIfPresent(Object)}）
     * @return 缓存值或 loader 的返回值；均无值时返回 {@code null}
     */
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
     * @param isUpdateCount      是否更新命中/丢失计数器
     * @return 值对象；不存在或已过期返回 {@code null}
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

    /**
     * 是否包含 key
     * <p>
     * 已过期数据会被移除并回调监听器；不更新最后访问时间，也不计入命中/丢失统计。
     *
     * @param key 键，可为 {@code null}
     * @return 存在且未过期返回 {@code true}
     */
    @Override
    public boolean containsKey(K key) {
        return null != getOrRemoveExpired(key, false, false);
    }

    /**
     * 返回有效数据的 键-值 快照
     * <p>
     * 过滤已过期数据但不会移除它们，也不触发监听器；返回新的 Map，修改不影响缓存。
     * value 为 {@code null} 时底层 {@code Collectors.toMap} 会抛出 {@link NullPointerException}。
     *
     * @return 键-值 映射，无有效数据时返回空 Map
     */
    @Override
    public Map<K, V> asMap() {
        return this.cacheMap
                .values()
                .stream()
                .filter(cacheObject -> !cacheObject.isExpired())
                .collect(Collectors.toMap(CacheObject::getKey, CacheObject::getValue));
    }

    /**
     * 缓存条目数量，包含尚未清理的过期数据
     *
     * @return 条目数量
     */
    @Override
    public int size() {
        return cacheMap.size();
    }

    /**
     * 缓存是否已满，条件为 {@code capacity > 0 && size() >= capacity}，包含过期数据
     *
     * @return 已达到容量上限返回 {@code true}；无容量限制时始终返回 {@code false}
     */
    @Override
    public boolean isFull() {
        return (capacity > 0) && (cacheMap.size() >= capacity);
    }

    /**
     * 缓存是否为空，包含尚未清理的过期数据
     *
     * @return 无任何条目时返回 {@code true}
     */
    @Override
    public boolean isEmpty() {
        return cacheMap.isEmpty();
    }

    // ---------------------- delete ----------------------

    /**
     * 删除指定 key 的缓存，并在删除成功后回调监听器
     *
     * @param key 键，可为 {@code null}
     */
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
     * <p>
     * 调用方需已持有该 key 对应的分桶锁；本方法不触发监听器。
     *
     * @param key 键
     * @return 移除的对象，无返回null
     */
    protected CacheObject<K, V> removeWithoutLock(K key) {
        return cacheMap.remove(CacheKey.of(key));
    }

    /**
     * 对象移除回调
     * <p>
     * 未设置监听器或对象为 {@code null} 时忽略；监听器异常被捕获并记录日志，不影响缓存操作。
     *
     * @param cacheObject 被缓存的对象，可为 {@code null}
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

    /**
     * 清理过期数据并按具体策略处理容量，使用 null key 对应的桶锁保证并发安全
     *
     * @return 本次清理（含过期移除与容量淘汰）的条目数量
     */
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
     * 子类实现此方法时无需加锁；调用方（{@link #prune()}）已持有 null key 对应的桶锁。
     * 清理过期或淘汰数据时应调用 {@link #onRemove(CacheObject)} 保持监听语义一致。
     *
     * @return 清理数据量
     */
    protected abstract int doPrune();

    /**
     * 清空缓存数据
     * <p>
     * 不会逐条触发监听器，也不会重置命中/丢失计数。
     */
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
     * 命中次数：统计 {@code get} 系列方法的命中，{@code containsKey} 不计入
     *
     * @return 命中数
     */
    public long hitCount() {
        return hitCount.sum();
    }

    /**
     * 未命中次数：统计 {@code get} 系列方法的未命中，{@code containsKey} 不计入
     *
     * @return 丢失数
     */
    public long missCount() {
        return missCount.sum();
    }

    // ---------------------- info ----------------------

    /**
     * 缓存容量上限
     *
     * @return 容量，{@code 0} 表示无限制
     */
    @Override
    public int capacity() {
        return capacity;
    }

    /**
     * 缓存有效期
     *
     * @return 有效期，单位毫秒，{@code 0} 表示永不过期
     */
    @Override
    public long timeout() {
        return timeout;
    }

    // ---------------------- listener ----------------------

    /**
     * 设置删除监听器，后续删除、过期清理与容量淘汰时回调
     *
     * @param listener 监听器，可为 {@code null}
     * @return 当前缓存实例
     */
    @Override
    public ReentrantCache<K, V> setListener(CacheListener<K, V> listener) {
        this.listener = listener;
        return this;
    }

    /**
     * 设置默认加载器，供 {@link #get(Object)} 未命中时使用
     *
     * @param loader 加载器，可为 {@code null}
     * @return 当前缓存实例
     */
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

    /**
     * 返回底层缓存的字符串表示，用于调试
     * <p>
     * 输出内容可能包含已过期但尚未清理的条目，不建议用于业务判断。
     *
     * @return 缓存内容字符串
     */
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