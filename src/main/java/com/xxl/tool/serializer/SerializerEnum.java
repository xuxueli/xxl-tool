package com.xxl.tool.serializer;

import com.xxl.tool.serializer.impl.JavaSerializer;

/**
 * 序列化器枚举，作为 {@link Serializer} 实现的简单注册表
 *
 * <p>当前仅提供 {@link #JAVA}（JDK 原生序列化）。枚举与内置实例不可变，
 * 线程安全；具体 {@link Serializer} 是否线程安全由实现决定。</p>
 */
public enum SerializerEnum {

    /** JDK 原生序列化，要求对象实现 {@link java.io.Serializable} */
    JAVA(new JavaSerializer());
    // FASTJSON-JSONB、JDK、FST、Kryo、Protobuf、Thrift、Hession、Avro

    private final Serializer serializer;

    /**
     * 构造枚举项
     *
     * @param serializer 对应的序列化器实例
     */
    SerializerEnum(Serializer serializer) {      // Class<? extends Serializer> serializerClass
        this.serializer = serializer;
    }

    /**
     * 获取序列化器实例
     *
     * @return 当前枚举项对应的序列化器
     */
    public Serializer getSerializer() {
        return serializer;
    }

    /**
     * 按枚举名匹配序列化器
     *
     * @param name              枚举名（区分大小写），可为 {@code null}
     * @param defaultSerializer 未匹配时的默认枚举，可为 {@code null}
     * @return 匹配的枚举项；未匹配时返回 {@code defaultSerializer}
     */
    public static SerializerEnum match(String name, SerializerEnum defaultSerializer) {
        for (SerializerEnum item : SerializerEnum.values()) {
            if (item.name().equals(name)) {
                return item;
            }
        }
        return defaultSerializer;
    }

}