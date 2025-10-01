package com.xxl.tool.cache.impl;

import com.xxl.tool.cache.iface.Cache;
import com.xxl.tool.cache.iface.CacheLoader;

import java.util.concurrent.ConcurrentMap;

/**
 * No Cache
 *
 * @param <K>
 * @param <V>
 */
public class NoCache<K, V> implements Cache<K, V> {
    private static final long serialVersionUID = 42L;

    // ---------------------- put ----------------------

    @Override
    public void put(K key, V object) {
    }

    // ---------------------- get ----------------------

    @Override
    public V get(K key) {
        return null;
    }

    @Override
    public V getIfPresent(K key) {
        return null;
    }

    @Override
    public V get(K key, CacheLoader<K, V> cacheLoader) {
        try {
            return (null == cacheLoader) ? null : cacheLoader.load(key);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ---------------------- query ----------------------

    @Override
    public boolean containsKey(K key) {
        return false;
    }

    @Override
    public ConcurrentMap<K, V> asMap() {
        return null;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isFull() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }


    // ---------------------- delete ----------------------

    @Override
    public void remove(K key) {
    }

    @Override
    public int prune() {
        return 0;
    }

    @Override
    public void clear() {
    }

    // ---------------------- monitor ----------------------

    @Override
    public long hitCount() {
        return 0;
    }

    @Override
    public long missCount() {
        return 0;
    }

    // ---------------------- info ----------------------

    @Override
    public int capacity() {
        return 0;
    }

    @Override
    public long timeout() {
        return 0;
    }

}