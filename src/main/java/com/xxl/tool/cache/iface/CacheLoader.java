package com.xxl.tool.cache.iface;

/**
 * 缓存加载器：在 {@link Cache#get(Object)} 或 {@link Cache#get(Object, CacheLoader)} 未命中时回调，
 * 由使用方负责生产并返回值对象
 *
 * <p>默认实现中回调在对应 key 的写锁内执行，同一 key 的并发未命中只会有一个线程真正加载；
 * 回调抛出的受检异常会被缓存实现包装为 {@link RuntimeException} 抛出，且异常时不会写入缓存。
 * 实现类应保证线程安全，并避免在回调中递归访问同一个缓存实例。</p>
 *
 * @param <K> 键类型
 * @param <V> 值类型
 */
public abstract class CacheLoader<K, V> {

    /**
     * 加载缓存值
     *
     * @param key 未命中的键，可为 {@code null}
     * @return 加载到的值；返回 {@code null} 时缓存实现不会写入该条目
     * @throws Exception 加载失败时抛出，由缓存实现包装为 {@link RuntimeException}
     */
    public abstract V load(K key) throws Exception;

}