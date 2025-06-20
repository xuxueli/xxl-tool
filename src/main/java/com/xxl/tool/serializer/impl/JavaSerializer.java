package com.xxl.tool.serializer.impl;

import com.xxl.tool.serializer.Serializer;

import java.io.*;

/**
 * serializer
 *
 * @author xuxueli 2025-02-07
 */
public class JavaSerializer extends Serializer {

    /**
     * serialize
     *
     * @param obj
     * @return
     * @param <T>
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
