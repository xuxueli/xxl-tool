package com.xxl.tool.cache.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Cache Key
 */
public class CacheKey<K> implements Serializable {
    private static final long serialVersionUID = 42L;

    // ---------------------- tool ----------------------

    public static <K> CacheKey<K> of(K key) {
        return new CacheKey<>(key);
    }

    // ---------------------- field ----------------------

    /**
     * key
     */
    private K key;

    public CacheKey(final K key) {
        this.key = key;
    }

    public K get() {
        return this.key;
    }

    public void set(final K key) {
        this.key = key;
    }

    // ---------------------- other ----------------------

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (this.getClass() == obj.getClass()) {
            final CacheKey<?> that = (CacheKey<?>) obj;
            return Objects.equals(this.key, that.key);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.key == null ? 0 : this.key.hashCode();
    }

    @Override
    public String toString() {
        return this.key == null ? "null" : this.key.toString();
    }

}