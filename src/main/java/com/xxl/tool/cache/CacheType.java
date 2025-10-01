package com.xxl.tool.cache;

/**
 * Cache Type
 */
public enum CacheType {

	/**
	 * None
	 */
    NONE,

	/**
	 * First In First Out
	 */
    FIFO,

	/**
	 * Least Frequently Used
	 */
    LFU,

	/**
	 * Least Recently Used
	 */
    LRU,

	/**
	 * Unlimited Size
	 */
	UNLIMITED;

}