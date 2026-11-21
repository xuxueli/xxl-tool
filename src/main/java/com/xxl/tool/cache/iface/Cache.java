package com.xxl.tool.cache.iface;

import java.io.Serializable;
import java.util.Map;

/**
 * 缓存接口：定义本地缓存的读写、查询、清理与监控契约
 *
 * <p>通用语义约定：</p>
 * <ul>
 *   <li>有效期 {@code timeout} 单位为毫秒，{@code 0} 表示永不过期；</li>
 *   <li>容量 {@code capacity} 为 {@code 0} 表示不限制容量；</li>
 *   <li>过期数据采用惰性删除：读取时发现过期会移除，{@link #size()}、{@link #isEmpty()}、
 *       {@link #isFull()} 仍可能把过期数据统计在内，{@link #asMap()} 则会过滤过期数据；</li>
 *   <li>是否线程安全由具体实现决定；{@code ReentrantCache} 系列实现通过分桶锁保证写入与过期删除的线程安全，
 *       查询结果（如 {@link #asMap()}、{@link #size()}）为弱一致快照。</li>
 * </ul>
 *
 * @param <K> 键类型
 * @param <V> 值类型
 */
public interface Cache<K, V> extends Serializable {

    // ---------------------- put ----------------------

    /**
     * 设置缓存对象
     *
     * <p>key 为 {@code null} 时直接忽略，不写入缓存；value 允许为 {@code null}，
     * 但读取时无法与“键不存在”区分。该方法会重置该键的写入时间与最后访问时间，并重新计算有效期。</p>
     *
     * @param key    键，可为 {@code null}（忽略）
     * @param object 值对象，可为 {@code null}
     */
    void put(K key, V object);

    // ---------------------- get ----------------------

    /**
     * 获取缓存对象，未命中时使用缓存内置的 {@link CacheLoader} 加载
     * <p>
     * 1、缓存对象不存在 或 已过期：尝试使用 cacheLoader 加载并返回对象，如果 cacheLoader 不存在返回 {@code null}
     * 2、命中时刷新最后访问时间，即 expireAfterAccess 策略下重新计算超时时间；
     *    expireAfterWrite 策略下过期时间仍以写入时间为准，刷新访问时间不会延长有效期
     * 3、未命中会执行 loader，loader 抛出的异常包装为 {@link RuntimeException} 后抛出；
     *    loader 返回 {@code null} 时不会得到有效缓存值（具体实现可能写入 null，随后读取仍按未命中处理）
     *
     * @param key 键，可为 {@code null}（按未命中处理）
     * @return 缓存值；未命中且无 loader、或 loader 返回 {@code null} 时返回 {@code null}
     */
    V get(K key);


    /**
     * 获取缓存对象，仅查询已有数据，不触发 {@link CacheLoader}
     * <p>
     * 1、缓存对象不存在 或 已过期：直接返回 {@code null}
     * 2、命中时刷新最后访问时间，即 expireAfterAccess 策略下重新计算超时时间；
     *    expireAfterWrite 策略下刷新访问时间不会延长有效期
     *
     * @param key 键，可为 {@code null}（按未命中处理）
     * @return 缓存值；不存在或已过期返回 {@code null}
     */
    V getIfPresent(K key);

    /**
     * 获取缓存对象，未命中时使用指定 loader 加载
     * <p>
     * 1、缓存对象不存在 或 已过期：使用 cacheLoader 加载并返回对象；cacheLoader 为 {@code null} 时行为同
     *    {@link #getIfPresent(Object)}
     * 2、loader 在对应 key 的锁内执行，同一 key 并发未命中时只会有一个线程真正加载；
     *    加载成功后通过 {@link #put(Object, Object)} 写入缓存（缓存已满时会先触发淘汰）
     * 3、loader 抛出的异常包装为 {@link RuntimeException} 后抛出；loader 返回 {@code null} 时不会得到有效缓存值
     *    （具体实现可能写入 null，随后读取仍按未命中处理）
     *
     * @param key         键，可为 {@code null}（按未命中处理）
     * @param cacheLoader 回调方法，用于生产值对象，可为 {@code null}
     * @return 缓存值；未命中且未加载到值时返回 {@code null}
     */
    V get(K key, CacheLoader<K, V> cacheLoader);

    // ---------------------- query ----------------------

    /**
     * 是否包含 key
     * <p>
     * 已过期的数据视为不存在，并在判断过程中移除该数据、触发 {@link CacheListener#onRemove(Object, Object)}；
     * 该调用不更新命中/丢失计数。
     *
     * @param key 键，可为 {@code null}
     * @return 存在且未过期返回 {@code true}，否则返回 {@code false}
     */
    boolean containsKey(K key);

    /**
     * 返回 键-值 映射快照，仅包括有效数据
     * <p>
     * 1、已过期的数据会被过滤，但不会因此被移除，也不会触发 {@link CacheListener#onRemove(Object, Object)}；
     * 2、返回的是新创建的 Map，修改它不会影响缓存本身；value 为 {@code null} 的条目可能被过滤、
     *    导致返回 {@code null} 或抛出 {@link NullPointerException}，具体取决于实现
     *
     * @return 有效的 键-值 映射；无有效数据时通常返回空 Map，空缓存实现可能返回 {@code null}
     */
    Map<K, V> asMap();

    /**
     * 缓存对象数量
     * <p>
     * 1、包含过期数据（过期数据在读取或清理时才被移除）
     *
     * @return 缓存中的条目数量
     */
    int size();

    /**
     * 缓存是否已满，仅用于有空间限制的缓存对象
     * <p>
     * 1、包含过期数据
     * 2、判断条件为 {@code capacity > 0 && size() >= capacity}，无容量限制的缓存始终返回 {@code false}
     *
     * @return 已达到容量上限返回 {@code true}，否则返回 {@code false}
     */
    boolean isFull();

    /**
     * 缓存是否为空
     * <p>
     * 1、包含过期数据，过期但尚未清理的数据仍会使该方法返回 {@code false}
     *
     * @return 无任何条目（含过期条目）返回 {@code true}
     */
    boolean isEmpty();

    // ---------------------- delete ----------------------

    /**
     * 删除单个缓存对象
     * <p>
     * 删除成功会触发 {@link CacheListener#onRemove(Object, Object)}；key 不存在时不做任何处理。
     *
     * @param key 键，可为 {@code null}
     */
    void remove(K key);

    /**
     * 清理过期缓存对象
     * <p>
     * 可手动调用，也可由 {@code CacheTool#pruneInterval(long)} 注册的定时任务调用；
     * 被清理或淘汰的条目会触发 {@link CacheListener#onRemove(Object, Object)}。
     *
     * @return 本次清理的条目数量
     */
    int prune();

    /**
     * 清空缓存对象
     * <p>
     * 仅清空数据，不重置 {@link #hitCount()}、{@link #missCount()} 统计；
     * 当前实现不会对每条数据触发 {@link CacheListener#onRemove(Object, Object)}。
     */
    void clear();

    // ---------------------- monitor ----------------------

    /**
     * 统计 {@link #get(Object)}、{@link #getIfPresent(Object)}、{@link #get(Object, CacheLoader)}
     * 的命中次数，{@link #containsKey(Object)} 不计入。
     *
     * @return 命中数
     */
    long hitCount();

    /**
     * 统计 {@link #get(Object)}、{@link #getIfPresent(Object)}、{@link #get(Object, CacheLoader)}
     * 的未命中次数（loader 二次确认命中不重复计数），{@link #containsKey(Object)} 不计入。
     *
     * @return 丢失数
     */
    long missCount();

    // ---------------------- info ----------------------

    /**
     * 缓存容量, {@code 0} 表示无限制
     *
     * @return 容量上限
     */
    int capacity();

    /**
     * 缓存有效期，{@code 0} 表示无限制，单位毫秒
     *
     * @return 有效期（毫秒）
     */
    long timeout();

    // ---------------------- listener ----------------------

    /**
     * 设置删除监听器，用于在条目被移除（手动删除、过期清理、容量淘汰）时回调
     * <p>
     * 默认实现不做任何处理并返回当前实例；具体实现通常返回 {@code this} 以支持链式调用。
     *
     * @param listener 监听器，可为 {@code null}（表示不监听）
     * @return 当前缓存实例
     */
    default Cache<K, V> setListener(CacheListener<K, V> listener) {
        return this;
    }

    // ---------------------- loader ----------------------

    /**
     * 设置缓存加载器，用于 {@link #get(Object)} 未命中时加载数据
     * <p>
     * 默认实现不做任何处理并返回当前实例；具体实现通常返回 {@code this} 以支持链式调用。
     *
     * @param listener 加载器，可为 {@code null}（表示不加载）
     * @return 当前缓存实例
     */
    default Cache<K, V> setLoader(CacheLoader<K, V> listener) {
        return this;
    }

}