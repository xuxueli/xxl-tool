package com.xxl.tool.cache.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * 缓存键包装：将业务 key 包装为可序列化的 Map 键，{@code equals}/{@code hashCode} 均委托给内部 key
 *
 * <p>作为缓存内部 Map 的键使用；内部 key 可变，修改后会导致 {@code hashCode} 变化，
 * 已放入 Map 的条目将无法再被查找，因此放入缓存后不应再调用 {@link #set(Object)}。</p>
 *
 * <p>线程安全：不可变使用（不调用 setter）时线程安全；setter 与读取并发时无同步保证。
 * 生命周期：无资源需要关闭。</p>
 *
 * @param <K> 键类型
 */
public class CacheKey<K> implements Serializable {
    private static final long serialVersionUID = 42L;

    // ---------------------- tool ----------------------

    /**
     * 创建缓存键包装
     *
     * @param key 原始键，可为 {@code null}
     * @param <K> 键类型
     * @return 包装后的缓存键
     */
    public static <K> CacheKey<K> of(K key) {
        return new CacheKey<>(key);
    }

    // ---------------------- field ----------------------

    /**
     * 原始业务键，可为 {@code null}
     *
     * <p>作为 Map 键使用后不应再修改；修改会改变 {@code hashCode}，导致原条目无法查找。</p>
     */
    private K key;

    /**
     * 构造缓存键包装
     *
     * @param key 原始键，可为 {@code null}
     */
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

    /**
     * 仅当类型相同且内部 key 相等时视为相等
     *
     * @param obj 比较对象
     * @return 相等返回 {@code true}
     */
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

    /**
     * 哈希值，与内部 key 的哈希值一致；key 为 {@code null} 时返回 0
     *
     * @return 哈希值
     */
    @Override
    public int hashCode() {
        return this.key == null ? 0 : this.key.hashCode();
    }

    /**
     * 字符串表示，直接返回内部 key 的字符串
     *
     * @return key 为 {@code null} 时返回字符串 {@code "null"}
     */
    @Override
    public String toString() {
        return this.key == null ? "null" : this.key.toString();
    }

}