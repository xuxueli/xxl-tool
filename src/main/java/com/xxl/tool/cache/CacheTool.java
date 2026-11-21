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
 * 缓存构建工具：以链式参数创建 {@link Cache} 实例
 *
 * <p>典型用法：</p>
 * <pre>
 * CacheTool&lt;String, Object&gt; cacheTool = CacheTool.&lt;String, Object&gt;newLRUCache(1000)
 *         .expireAfterAccess(60 * 1000)
 *         .pruneInterval(30 * 1000);
 * Cache&lt;String, Object&gt; cache = cacheTool.build();
 * // 不再使用时取消定时清理任务（缓存数据本身无需关闭）
 * cacheTool.stop();
 * </pre>
 *
 * <p>默认配置：{@link #newCache()} 使用 LRU、容量 1000、永不过期；未显式设置过期时间时 {@code timeout = 0}，
 * 表示数据永不过期。{@link #build()} 之前可反复调整参数，构建后再修改构建器不会影响已创建的缓存实例。</p>
 *
 * <p>线程安全：构建器本身非线程安全，应在单线程内完成配置；构建出的缓存是否线程安全由 {@link CacheType} 对应实现决定，
 * FIFO/LRU/LFU/UNLIMITED 均基于 {@code ReentrantCache}，写入与过期删除通过分桶锁保证线程安全，查询为弱一致快照。</p>
 *
 * <p>生命周期：{@link #build()} 在 {@code pruneInterval > 0} 时会向全局单线程调度器注册周期清理任务，
 * 需要调用 {@link #stop()} 取消；未取消的任务会随 JVM 退出（ShutdownHook）停止。
 * 清理任务对异常只记录日志，不会终止后续调度。</p>
 *
 * @param <K>	缓存键类型
 * @param <V>	缓存值类型
 * @author xuxueli 2018-01-22
 */
public class CacheTool<K, V> {
    private static final Logger logger = LoggerFactory.getLogger(CacheTool.class);

    // ---------------------- build ----------------------

    /**
     * 创建默认缓存：LRU 策略、容量 1000、永不过期
     *
     * @param <K> 键类型
     * @param <V> 值类型
     * @return 缓存构建器
     */
    public static <K, V> CacheTool<K, V> newCache() {
        return newLRUCache();
    }

    /**
     * 创建默认容量的 FIFO 缓存构建器：容量 1000、永不过期
     *
     * @param <K> 键类型
     * @param <V> 值类型
     * @return 缓存构建器
     */
    public static <K, V> CacheTool<K, V> newFIFOCache() {
        return newFIFOCache(1000);
    }

    /**
     * 创建指定容量的 FIFO 缓存构建器：先进先出淘汰、永不过期
     *
     * @param capacity 容量上限，必须大于 0；{@code build()} 时会校验，非法值抛 {@link IllegalArgumentException}
     * @param <K>      键类型
     * @param <V>      值类型
     * @return 缓存构建器
     */
    public static <K, V> CacheTool<K, V> newFIFOCache(int capacity) {
        return new CacheTool<K, V>()
                .cache(CacheType.FIFO)
                .capacity(capacity);
    }

    /**
     * 创建默认容量的 LRU 缓存构建器：容量 1000、永不过期
     *
     * @param <K> 键类型
     * @param <V> 值类型
     * @return 缓存构建器
     */
    public static <K, V> CacheTool<K, V> newLRUCache() {
        return newLRUCache(1000);
    }

    /**
     * 创建指定容量的 LRU 缓存构建器：淘汰最久未使用的数据、永不过期
     *
     * @param capacity 容量上限，必须大于 0；{@code build()} 时会校验，非法值抛 {@link IllegalArgumentException}
     * @param <K>      键类型
     * @param <V>      值类型
     * @return 缓存构建器
     */
    public static <K, V> CacheTool<K, V> newLRUCache(int capacity) {
        return new CacheTool<K, V>()
                .cache(CacheType.LRU)
                .capacity(capacity);
    }

    /**
     * 创建默认容量的 LFU 缓存构建器：容量 1000、永不过期
     *
     * @param <K> 键类型
     * @param <V> 值类型
     * @return 缓存构建器
     */
    public static <K, V> CacheTool<K, V> newLFUCache() {
        return newLFUCache(1000);
    }

    /**
     * 创建指定容量的 LFU 缓存构建器：淘汰访问次数最少的数据、永不过期
     *
     * @param capacity 容量上限，必须大于 0；{@code build()} 时会校验，非法值抛 {@link IllegalArgumentException}
     * @param <K>      键类型
     * @param <V>      值类型
     * @return 缓存构建器
     */
    public static <K, V> CacheTool<K, V> newLFUCache(int capacity) {
        return new CacheTool<K, V>()
                .cache(CacheType.LFU)
                .capacity(capacity);
    }

    /**
     * 创建无容量限制缓存构建器：不限制容量、永不过期
     *
     * @param <K> 键类型
     * @param <V> 值类型
     * @return 缓存构建器
     */
    public static <K, V> CacheTool<K, V> newUnlimitedCache() {
        return newUnlimitedCache(0);
    }

    /**
     * 创建无容量限制、指定有效期的缓存构建器
     * <p>
     * 有效期按 expireAfterWrite 计算，从写入时间开始计时。
     *
     * @param timeout 有效期，单位毫秒；{@code 0} 表示永不过期
     * @param <K>     键类型
     * @param <V>     值类型
     * @return 缓存构建器
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
     * 设置过期时长并采用 expireAfterWrite 策略：从写入时间开始计算，到期即失效
     * <p>
     * 访问不会延长有效期；再次 {@code put} 同一 key 会刷新写入时间。
     *
     * @param timeout 有效期，单位毫秒；{@code 0} 表示永不过期
     * @return 当前构建器，便于链式调用
     */
    public CacheTool<K, V> expireAfterWrite(long timeout) {
        this.timeout = timeout;
        this.expireType = true;
        return this;
    }

    /**
     * 设置过期时长并采用 expireAfterAccess 策略：从最后一次访问时间开始计算，到期即失效
     * <p>
     * 每次命中（{@code get} / {@code getIfPresent}）都会刷新最后访问时间。
     *
     * @param timeout 有效期，单位毫秒；{@code 0} 表示永不过期
     * @return 当前构建器，便于链式调用
     */
    public CacheTool<K, V> expireAfterAccess(long timeout) {
        this.timeout = timeout;
        this.expireType = false;
        return this;
    }

    /**
     * 设置缓存容量
     * <p>
     * 仅对 FIFO/LRU/LFU 生效，{@code 0} 表示不限制容量；无容量限制的缓存类型会忽略该值。
     *
     * @param capacity 容量上限，非负；FIFO/LRU/LFU 要求大于 0，否则 {@code build()} 抛 {@link IllegalArgumentException}
     * @return 当前构建器，便于链式调用
     */
    public CacheTool<K, V> capacity(int capacity) {
        this.capacity = capacity;
        return this;
    }

    /**
     * 设置缓存类型，覆盖工厂方法创建的默认类型
     *
     * @param cacheType 缓存类型，{@code null} 或未识别类型会在 {@code build()} 时抛 {@link RuntimeException}
     * @return 当前构建器，便于链式调用
     */
    public CacheTool<K, V> cache(CacheType cacheType) {
        this.cacheType = cacheType;
        return this;
    }

    /**
     * 设置缓存对象创建组件，用于 {@code get} 未命中时加载数据
     *
     * @param cacheLoader 加载器，可为 {@code null}；为 {@code null} 时未命中直接返回 {@code null}
     * @return 当前构建器，便于链式调用
     */
    public CacheTool<K, V> loader(CacheLoader<K, V> cacheLoader) {
        this.loader = cacheLoader;
        return this;
    }

    /**
     * 设置缓存变更（删除）监听器
     * <p>
     * 条目被手动删除、过期清理或容量淘汰时回调，回调异常只记录日志。
     *
     * @param listener 监听器，可为 {@code null}（表示不监听）
     * @return 当前构建器，便于链式调用
     */
    public CacheTool<K, V> listener(CacheListener<K, V> listener) {
        this.listener = listener;
        return this;
    }

    /**
     * 设置定时清理延时，以固定频率调用 {@link Cache#prune()}
     * <p>
     * 未设置时不会注册定时任务；UNLIMITED 类型在未设置（{@code <= 0}）时会自动启用 5 秒的清理周期。
     * 定时任务异常只记录日志，不影响后续调度。
     *
     * @param pruneInterval 清理周期，单位毫秒；{@code 0} 表示不启用定时清理
     * @return 当前构建器，便于链式调用
     */
    public CacheTool<K, V> pruneInterval(long pruneInterval) {
        this.pruneInterval = pruneInterval;
        return this;
    }

    /**
     * 构建缓存实例
     * <p>
     * 每次调用都会创建新的缓存实例；{@code pruneInterval > 0} 时同时注册周期清理任务。
     *
     * @param <K> 键类型
     * @param <V> 值类型
     * @return 构建完成的缓存实例，线程安全语义见具体实现
     * @throws IllegalArgumentException FIFO/LRU/LFU 的容量小于等于 0
     * @throws RuntimeException         缓存类型为 {@code null} 或不受支持
     */
    @SuppressWarnings("unchecked")
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
            // 未指定或不受支持的缓存类型
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
     * 停止当前构建器注册的定时清理任务
     * <p>
     * 可重复调用；仅取消定时清理，不会清空缓存数据，也不影响其他构建器注册的任务。
     * 未调用时任务会随 JVM 退出（ShutdownHook）停止。
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
    /**
     * 全局定时清理线程池：单线程，由所有 CacheTool 实例共享，JVM 退出时通过 ShutdownHook 关闭
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