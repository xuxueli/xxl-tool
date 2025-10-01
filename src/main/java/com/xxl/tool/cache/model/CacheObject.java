package com.xxl.tool.cache.model;

import com.xxl.tool.core.DateTool;

import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Cache Object
 */
public class CacheObject<K, V> implements Serializable {
    private static final long serialVersionUID = 42L;

    /**
     * key
     */
    private final K key;

    /**
     * value
     */
    private final V value;

    /**
     * 上次访问时间
     */
    private volatile long lastAccess;

    /**
     * 创建时间
     */
    private volatile long createTime;

    /**
     * 过期策略，true 表示 expireAfterAccess，false 表示 expireAfterWrite
     */
    private volatile boolean expireType;

    /**
     * 访问次数
     */
    private final AtomicLong accessCount = new AtomicLong();

    /**
     * 超时/存活时长，0 表示永久存活
     */
    private final long ttl;

    /**
     * 构造
     *
     * @param key   键
     * @param value 值
     * @param ttl   超时时长
     * @param expireType   过期策略，true 表示 expireAfterAccess，false 表示 expireAfterWrite
     */
    public CacheObject(K key, V value, long ttl, boolean expireType) {
        this.key = key;
        this.value = value;
        this.ttl = ttl;
        this.createTime = System.currentTimeMillis();
        this.lastAccess = System.currentTimeMillis();
        this.expireType = expireType;
    }

    public K getKey() {
        return this.key;
    }

    public V getValue() {
        return this.value;
    }

    public long getTtl() {
        return this.ttl;
    }

    /**
     * 获取过期时间，返回 {@code null} 表示永不过期
     */
    public Date getExpiredTime() {
        if (this.ttl > 0) {
            return new Date(this.lastAccess + this.ttl);
        }
        return null;
    }

    /**
     * 获取上次访问时间
     */
    public long getLastAccess() {
        return this.lastAccess;
    }


    @Override
    public String toString() {
        return "CacheObject{" +
                "key=" + key +
                ", value=" + value +
                ", lastAccess=" + lastAccess +
                ", createTime=" + createTime +
                ", accessCount=" + accessCount +
                ", ttl=" + ttl +
                '}';
    }

    /**
     * 判断是否过期
     */
    public boolean isExpired() {
        if (this.ttl > 0) {
            if (expireType) {
                // expireAfterAccess
                return (System.currentTimeMillis() - this.createTime) > this.ttl;
            } else {
                // expireAfterWrite
                return (System.currentTimeMillis() - this.lastAccess) > this.ttl;
            }
        }
        return false;
    }

    /**
     * 获取值
     *
     * @param isUpdateLastAccess 是否更新最后访问时间
     * @return 获得对象
     */
    public V get(boolean isUpdateLastAccess) {
        if (isUpdateLastAccess) {
            lastAccess = System.currentTimeMillis();
        }
        accessCount.getAndIncrement();
        return this.value;
    }

	/**
     * 获取访问次数
     */
    public AtomicLong getAccessCount() {
        return accessCount;
    }

}