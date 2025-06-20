package com.xxl.tool.serializer;

/**
 * serializer
 *
 * @author xuxueli 2025-02-07
 */
public abstract class Serializer {

	/**
	 * serialize
	 *
	 * @param obj
	 * @return
	 * @param <T>
	 */
	public abstract <T> byte[] serialize(T obj);

	/**
	 * deserialize
	 *
	 * @param bytes
	 * @return
	 * @param <T>
	 */
	public abstract <T> T deserialize(byte[] bytes);

}