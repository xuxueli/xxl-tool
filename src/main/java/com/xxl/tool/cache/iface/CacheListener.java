package com.xxl.tool.cache.iface;

/**
 * Cache Listener
 *
 * @param <K>
 * @param <V>
 */
public abstract class CacheListener<K, V> {

    /**
     * on remove
     */
    public abstract void onRemove(K key, V value) throws Exception;

}