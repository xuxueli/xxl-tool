package com.xxl.tool.serializer;

/**
 * 序列化抽象 SPI
 *
 * <p>实现类负责在对象与字节数组之间转换；具体 {@code null} 语义、异常类型与线程安全
 * 由实现自行约定并在实现类 Javadoc 中说明。序列化结果只保证同一实现/版本内可反序列化，
 * 跨版本兼容性取决于实现（如 JDK 原生序列化依赖 {@code serialVersionUID} 与类结构）。</p>
 *
 * <p><b>安全提示：</b>反序列化不可信数据可能导致远程代码执行或拒绝服务，调用方必须
 * 限制输入来源，禁止直接反序列化外部输入。</p>
 *
 * @author xuxueli 2025-02-07
 */
public abstract class Serializer {

	/**
	 * 序列化对象
	 *
	 * @param obj 待序列化对象，{@code null} 是否允许由实现决定
	 * @return 序列化后的字节数组
	 * @param <T> 对象类型
	 * @throws RuntimeException 序列化失败或实现不接受该对象时抛出（具体类型见实现）
	 */
	public abstract <T> byte[] serialize(T obj);

	/**
	 * 反序列化对象
	 *
	 * @param bytes 序列化字节数组，{@code null} 是否允许由实现决定
	 * @return 反序列化后的对象
	 * @param <T> 目标对象类型（运行时强转，类型不符可能抛 {@link ClassCastException}）
	 * @throws RuntimeException 反序列化失败或数据格式不匹配时抛出（具体类型见实现）
	 */
	public abstract <T> T deserialize(byte[] bytes);

}