package com.xxl.tool.cache.iface;

/**
 * Cache Loader
 *
 * @param <K>
 * @param <V>
 */
public abstract class CacheLoader<K, V> {

    /**
     * load cache
     */
    public abstract V load(K key) throws Exception;

}