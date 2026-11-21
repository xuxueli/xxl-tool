package com.xxl.tool.cache.impl;

import com.xxl.tool.cache.iface.Cache;
import com.xxl.tool.cache.iface.CacheLoader;

import java.util.concurrent.ConcurrentMap;

/**
 * 空缓存实现：不存储任何数据，所有读取都视为未命中
 *
 * <p>典型用途：通过配置切换关闭缓存能力时保持调用方代码不变。{@link #get(Object, CacheLoader)}
 * 仍会执行传入的 loader（不写入缓存），因此可用于“只加载不缓存”的场景。</p>
 *
 * <p>线程安全：实现无状态，可并发调用；但每次读取都会回调 loader，loader 自身需保证线程安全。</p>
 *
 * <p>生命周期：无需关闭或清理。入参不会被修改。</p>
 *
 * @param <K> 键类型
 * @param <V> 值类型
 */
public class NoCache<K, V> implements Cache<K, V> {
    private static final long serialVersionUID = 42L;

    // ---------------------- put ----------------------

    /**
     * 空实现：不存储任何数据
     *
     * @param key    键，忽略
     * @param object 值对象，忽略
     */
    @Override
    public void put(K key, V object) {
    }

    // ---------------------- get ----------------------

    /**
     * 固定未命中
     *
     * @param key 键，忽略
     * @return 固定返回 {@code null}
     */
    @Override
    public V get(K key) {
        return null;
    }

    /**
     * 固定未命中
     *
     * @param key 键，忽略
     * @return 固定返回 {@code null}
     */
    @Override
    public V getIfPresent(K key) {
        return null;
    }

    /**
     * 直接执行 loader 并返回结果，不写入缓存
     *
     * @param key         键，透传给 loader
     * @param cacheLoader 加载器；为 {@code null} 时返回 {@code null}
     * @return loader 的返回值；未提供 loader 时返回 {@code null}
     * @throws RuntimeException loader 抛出异常时包装为 {@link RuntimeException}
     */
    @Override
    public V get(K key, CacheLoader<K, V> cacheLoader) {
        try {
            return (null == cacheLoader) ? null : cacheLoader.load(key);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ---------------------- query ----------------------

    /**
     * 固定未命中
     *
     * @param key 键，忽略
     * @return 固定返回 {@code false}
     */
    @Override
    public boolean containsKey(K key) {
        return false;
    }

    /**
     * 空缓存没有数据可返回；当前实现返回 {@code null}，调用方需自行判空
     *
     * @return 固定返回 {@code null}
     */
    @Override
    public ConcurrentMap<K, V> asMap() {
        return null;
    }

    /**
     * 条目数量固定为 0
     *
     * @return 固定返回 {@code 0}
     */
    @Override
    public int size() {
        return 0;
    }

    /**
     * 空缓存不存在容量概念
     *
     * @return 固定返回 {@code false}
     */
    @Override
    public boolean isFull() {
        return false;
    }

    /**
     * 空缓存没有数据；当前实现固定返回 {@code false}（与“无数据即为空”的直觉相反），调用方需注意
     *
     * @return 固定返回 {@code false}
     */
    @Override
    public boolean isEmpty() {
        return false;
    }


    // ---------------------- delete ----------------------

    /**
     * 空实现：无数据可删除
     *
     * @param key 键，忽略
     */
    @Override
    public void remove(K key) {
    }

    /**
     * 空缓存无需清理
     *
     * @return 固定返回 {@code 0}
     */
    @Override
    public int prune() {
        return 0;
    }

    /**
     * 空实现：无数据可清空
     */
    @Override
    public void clear() {
    }

    // ---------------------- monitor ----------------------

    /**
     * 空缓存不记录命中
     *
     * @return 固定返回 {@code 0}
     */
    @Override
    public long hitCount() {
        return 0;
    }

    /**
     * 空缓存不记录未命中
     *
     * @return 固定返回 {@code 0}
     */
    @Override
    public long missCount() {
        return 0;
    }

    // ---------------------- info ----------------------

    /**
     * 空缓存无容量限制
     *
     * @return 固定返回 {@code 0}
     */
    @Override
    public int capacity() {
        return 0;
    }

    /**
     * 空缓存无有效期
     *
     * @return 固定返回 {@code 0}，表示永不过期
     */
    @Override
    public long timeout() {
        return 0;
    }

}