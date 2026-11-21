package com.xxl.tool.cache;

/**
 * 缓存类型枚举，用于 {@code CacheTool#cache(CacheType)} 选择淘汰策略
 *
 * <p>各类型对容量与淘汰行为的约定：</p>
 * <ul>
 *   <li>{@link #NONE}：不缓存任何数据，读操作直接走 loader；</li>
 *   <li>{@link #FIFO}：容量满时淘汰最早写入的数据；</li>
 *   <li>{@link #LFU}：容量满时淘汰访问次数最少的数据；</li>
 *   <li>{@link #LRU}：容量满时淘汰最久未使用的数据；</li>
 *   <li>{@link #UNLIMITED}：不限制容量，仅按有效期清理，需自行防止内存无限增长。</li>
 * </ul>
 *
 * <p>枚举本身无状态、线程安全；缓存实例的线程安全由具体实现保证。</p>
 */
public enum CacheType {

	/**
	 * 不使用缓存：{@code put} 忽略，{@code get(key, loader)} 直接执行传入的 loader（不缓存结果）
	 */
    NONE,

	/**
	 * First In First Out，先进先出：容量满时淘汰最早写入的数据
	 */
    FIFO,

	/**
	 * Least Frequently Used，最少使用：容量满时淘汰访问次数最少的数据
	 */
    LFU,

	/**
	 * Least Recently Used，最近最久未使用：容量满时淘汰最久未被访问的数据
	 */
    LRU,

	/**
	 * Unlimited Size，无容量限制：不会因容量触发淘汰，仅依赖有效期与定时清理
	 */
	UNLIMITED;

}