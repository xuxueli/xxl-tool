package com.xxl.tool.serializer;

import com.xxl.tool.serializer.impl.JavaSerializer;

public enum SerializerEnum {

    JAVA(new JavaSerializer());
    // FASTJSON-JSONB、JDK、FST、Kryo、Protobuf、Thrift、Hession、Avro

    private final Serializer serializer;

    SerializerEnum(Serializer serializer) {      // Class<? extends Serializer> serializerClass
        this.serializer = serializer;
    }

    public Serializer getSerializer() {
        return serializer;
    }

    /**
     * match serializer
     *
     * @param name
     * @param defaultSerializer
     * @return
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