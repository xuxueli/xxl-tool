package com.xxl.tool.cache.model;

import com.xxl.tool.core.DateTool;

import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 缓存值包装：保存键、值、写入时间、最后访问时间、过期策略与访问次数
 *
 * <p>过期语义：{@code ttl} 单位为毫秒，{@code 0} 表示永不过期；
 * {@code expireType = true} 为 expireAfterWrite（按写入时间判断），
 * {@code false} 为 expireAfterAccess（按最后访问时间判断）。</p>
 *
 * <p>线程安全：{@code lastAccess}/{@code createTime} 为 volatile，访问计数使用 {@link AtomicLong}；
 * {@link #isExpired()} 分别读取当前时间与时间戳，结果为近似判断，但不会抛异常。
 * 对象本身不负责加锁，由缓存实现保证读写并发安全。</p>
 *
 * <p>生命周期：仅作为数据载体，无需关闭；入参 key/value 不会被修改。</p>
 *
 * @param <K> 键类型
 * @param <V> 值类型
 */
public class CacheObject<K, V> implements Serializable {
    private static final long serialVersionUID = 42L;

    /**
     * key：缓存键，可为 {@code null}
     */
    private final K key;

    /**
     * value：缓存值，可为 {@code null}
     */
    private final V value;

    /**
     * 上次访问时间（毫秒时间戳）
     */
    private volatile long lastAccess;

    /**
     * 创建时间（毫秒时间戳），用于 expireAfterWrite 过期判断
     */
    private volatile long createTime;

    /**
     * 过期策略，true 表示 expireAfterWrite（按创建时间），false 表示 expireAfterAccess（按最后访问时间）
     */
    private volatile boolean expireType;

    /**
     * 访问次数，每次读取成功自增
     */
    private final AtomicLong accessCount = new AtomicLong();

    /**
     * 超时/存活时长，0 表示永久存活
     */
    private final long ttl;

    /**
     * 构造
     *
     * @param key   键，可为 {@code null}
     * @param value 值，可为 {@code null}
     * @param ttl   超时时长，单位毫秒；{@code 0} 表示永不过期
     * @param expireType   过期策略，true 表示 expireAfterWrite（按创建时间），false 表示 expireAfterAccess（按最后访问时间）
     */
    public CacheObject(K key, V value, long ttl, boolean expireType) {
        this.key = key;
        this.value = value;
        this.ttl = ttl;
        this.createTime = System.currentTimeMillis();
        this.lastAccess = System.currentTimeMillis();
        this.expireType = expireType;
    }

    /**
     * 获取键
     *
     * @return 缓存键，可能为 {@code null}
     */
    public K getKey() {
        return this.key;
    }

    /**
     * 获取值
     *
     * @return 缓存值，可能为 {@code null}
     */
    public V getValue() {
        return this.value;
    }

    /**
     * 获取有效期
     *
     * @return 有效期，单位毫秒；{@code 0} 表示永不过期
     */
    public long getTtl() {
        return this.ttl;
    }

    /**
     * 获取过期时间，返回 {@code null} 表示永不过期
     * <p>
     * 当前实现固定按 {@code lastAccess + ttl} 计算，因此 expireAfterWrite 场景下该结果偏大，
     * 与 {@link #isExpired()} 的实际判断点可能不一致，仅供展示参考。
     *
     * @return 过期时间点；{@code ttl <= 0} 时返回 {@code null}
     */
    public Date getExpiredTime() {
        if (this.ttl > 0) {
            return new Date(this.lastAccess + this.ttl);
        }
        return null;
    }

    /**
     * 获取上次访问时间
     *
     * @return 最后访问时间（毫秒时间戳）
     */
    public long getLastAccess() {
        return this.lastAccess;
    }


    /**
     * 返回包含 key、value、时间戳、访问次数与 ttl 的调试字符串
     *
     * @return 调试信息
     */
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
     * <p>
     * {@code ttl <= 0} 时永不过期；expireAfterWrite 按创建时间判断，expireAfterAccess 按最后访问时间判断。
     *
     * @return 已过期返回 {@code true}
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
     * <p>
     * 无论是否更新最后访问时间，都会使访问次数自增一次。
     *
     * @param isUpdateLastAccess 是否更新最后访问时间（expireAfterAccess 策略下会延长有效期）
     * @return 缓存值，可能为 {@code null}
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
     *
     * @return 访问次数计数器，可直接读写
     */
    public AtomicLong getAccessCount() {
        return accessCount;
    }

}