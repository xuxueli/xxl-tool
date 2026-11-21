package com.xxl.tool.cache.iface;

/**
 * 缓存删除监听器：在条目被移除时回调
 *
 * <p>回调触发时机包括：手动 {@link Cache#remove(Object)}、读取时发现过期并移除、{@link Cache#prune()} 清理过期数据、
 * 以及容量淘汰（FIFO 删除最早写入项、LFU 删除访问次数最少项、LRU 删除最久未使用项）。
 * {@link Cache#clear()} 不会逐条触发该回调。</p>
 *
 * <p>回调线程与锁：{@code put} 触发的容量淘汰、{@code prune()} 清理在缓存内部锁内执行；
 * {@link Cache#remove(Object)} 与读取时发现过期而移除的回调在锁释放后执行。
 * 实现应尽量轻量并避免反向调用同一缓存实例；回调抛出的异常会被缓存实现捕获并记录日志，不会中断缓存操作。</p>
 *
 * @param <K> 键类型
 * @param <V> 值类型
 */
public abstract class CacheListener<K, V> {

    /**
     * 条目被移除时回调
     *
     * @param key   被移除条目的键
     * @param value 被移除条目的值
     * @throws Exception 回调失败时抛出，由缓存实现捕获并记录日志
     */
    public abstract void onRemove(K key, V value) throws Exception;

}