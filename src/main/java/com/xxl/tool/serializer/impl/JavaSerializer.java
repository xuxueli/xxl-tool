package com.xxl.tool.serializer.impl;

import com.xxl.tool.serializer.Serializer;

import java.io.*;

/**
 * JDK 原生序列化实现，基于 {@link ObjectOutputStream} / {@link ObjectInputStream}
 *
 * <p>被序列化对象必须实现 {@link java.io.Serializable}；反序列化要求类路径中存在兼容的类定义，
 * 建议显式声明 {@code serialVersionUID} 以保证跨版本兼容。序列化结果只适用于 Java 进程间传输，
 * 不适合跨语言或长期存储。</p>
 *
 * <p><b>安全警告：</b>JDK 反序列化不可信数据可能被利用执行任意代码（gadget chain），
 * 严禁反序列化外部或未校验的字节；生产环境建议改用 JSON 等安全格式。</p>
 *
 * <p>实现无共享状态，线程安全（每次调用创建独立的流）；不修改入参。</p>
 *
 * @author xuxueli 2025-02-07
 */
public class JavaSerializer extends Serializer {

    /**
     * 序列化对象
     *
     * @param obj 待序列化对象，必须实现 {@link java.io.Serializable}，不可为 {@code null}
     * @return 序列化后的字节数组
     * @param <T> 对象类型
     * @throws RuntimeException 对象为 {@code null}、未实现 {@code Serializable} 或写入失败时抛出
     */
    @Override
    public <T> byte[] serialize(T obj) {
        if (obj == null) {
            throw new RuntimeException("Cannot serialize null object");
        }
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(obj);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize object: " + e.getMessage(), e);
        }
    }

    /**
     * 反序列化对象
     *
     * <p><b>安全警告：</b>仅可反序列化可信数据，禁止直接处理外部输入。</p>
     *
     * @param bytes 序列化字节数组，不可为 {@code null}
     * @return 反序列化后的对象（运行时强转为 T，类型不符可能抛 {@link ClassCastException}）
     * @param <T>   目标对象类型
     * @throws RuntimeException 字节数组为 {@code null}、格式非法、类不存在或读取失败时抛出
     */
    @Override
    public <T> T deserialize(byte[] bytes) {
        if (bytes == null) {
            throw new RuntimeException("Cannot deserialize null byte array");
        }
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            return (T) ois.readObject();
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize object: " + e.getMessage(), e);
        }
    }

}
