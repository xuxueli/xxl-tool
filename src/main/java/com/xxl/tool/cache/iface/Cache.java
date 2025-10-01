package com.xxl.tool.cache.iface;

import java.io.Serializable;
import java.util.Map;

/**
 * Cache Interface
 *
 * @param <K>
 * @param <V>
 */
public interface Cache<K, V> extends Serializable {

    // ---------------------- put ----------------------

    /**
     * 设置缓存对象
     *
     * @param key    健
     * @param object 值对象
     */
    void put(K key, V object);

    // ---------------------- get ----------------------

    /**
     * 获取缓存对象
     * <p>
     * 1、缓存对象不存在 或 已过期：尝试使用 cacheLoader 加载并返回对象，如果 cacheLoader 不存在返回 {@code null}
     * 2、每次调用此方法会刷新最后访问时间，即重新计算超时时间
     *
     * @param key 键
     * @return 值对象
     */
    V get(K key);


    /**
     * 获取缓存对象
     * <p>
     * 1、缓存对象不存在 或 已过期：直接返回 {@code null}
     * 2、每次调用此方法会刷新最后访问时间，即重新计算超时时间
     *
     * @param key 键
     * @return 值对象
     */
    V getIfPresent(K key);

    /**
     * 获取缓存对象
     * <p>
     * 1、缓存对象不存在 或 已过期：使用 cacheLoader 加载并返回对象
     * 2、每次调用此方法，可选是否 刷新最后访问时间，即重新计算超时时间
     *
     * @param key         键
     * @param cacheLoader 回调方法，用于生产值对象
     * @return 值对象
     */
    V get(K key, CacheLoader<K, V> cacheLoader);

    // ---------------------- query ----------------------

    /**
     * 是否包含key
     *
     * @param key 键
     * @return 值对象
     */
    boolean containsKey(K key);

    /**
     * 返回 键-值 映射，仅包括有效数据
     *
     * @return 键-值 映射（过滤已过期数据）
     */
    Map<K, V> asMap();

    /**
     * 缓存对象数量
     * <p>
     * 1、包含过期数据
     */
    int size();

    /**
     * 缓存是否已满，仅用于有空间限制的缓存对象
     * <p>
     * 1、包含过期数据
     */
    boolean isFull();

    /**
     * 缓存是否为空
     * <p>
     * 1、包含过期数据
     */
    boolean isEmpty();

    // ---------------------- delete ----------------------

    /**
     * 删除单个缓存对象
     *
     * @param key 键
     */
    void remove(K key);

    /**
     * 清理过期缓存对象
     */
    int prune();

    /**
     * 清空缓存对象
     */
    void clear();

    // ---------------------- monitor ----------------------

    /**
     * @return 命中数
     */
    long hitCount();

    /**
     * @return 丢失数
     */
    long missCount();

    // ---------------------- info ----------------------

    /**
     * 缓存容量, {@code 0} 表示无限制
     */
    int capacity();

    /**
     * 缓存有效期，{@code 0} 表示无限制，单位毫秒
     */
    long timeout();

    // ---------------------- listener ----------------------

    /**
     * 设置监听
     */
    default Cache<K, V> setListener(CacheListener<K, V> listener) {
        return this;
    }

    // ---------------------- listener ----------------------

    /**
     * 设置监听
     */
    default Cache<K, V> setLoader(CacheLoader<K, V> listener) {
        return this;
    }

}