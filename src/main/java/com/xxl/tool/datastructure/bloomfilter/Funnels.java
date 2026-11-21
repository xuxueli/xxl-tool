package com.xxl.tool.datastructure.bloomfilter;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Funnel 工具：定义「对象 → 字节」的转换契约，并提供常用类型的内置实现。
 *
 * <p>内置 Funnel 均为无状态单例常量，可被多线程共享；自定义 Funnel 必须保持确定性
 * （同一对象每次产生相同字节），否则布隆过滤器行为不可预期。{@link Funnel} 继承
 * {@link java.io.Serializable}，实现类应保持可序列化。
 *
 * @author xuxueli 2025-12-13
 */
public class Funnels {

    // ---------------------- funnel iface ----------------------

    /**
     * 对象到字节的转换器。
     *
     * @param <T> 对象类型
     */
    public interface Funnel<T extends Object> extends Serializable {

        /**
         * 把对象数据写入 hasher。
         *
         * @param from 源对象，提供待转换数据
         * @param into 目标接收者，用于接收数据，不能为 {@code null}
         */
        void funnel( T from, AbstractHasher into);

    }


    // ---------------------- funnel impl ----------------------

    /**
     * string funnel, default UTF-8
     */
    public static final Funnel<CharSequence> STRING;
    /**
     * unencoded chars funnel, directly write origin chars
     */
    public static final Funnel<CharSequence> UNENCODED_CHARS;
    /**
     * byte array funnel
     */
    public static final Funnel<byte[]> BYTE_ARRAY;
    /**
     * integer funnel
     */
    public static final Funnel<Integer> INTEGER;
    /**
     * long funnel
     */
    public static final Funnel<Long> LONG;

    static {
        STRING = new Funnel<>() {
            private final Charset charset = StandardCharsets.UTF_8;

            @Override
            public void funnel(CharSequence from, AbstractHasher into) {
                into.putString(from, charset);
            }

            @Override
            public String toString() {
                return "Funnels.stringFunnel(" + charset.name() + ")";
            }
        };
        UNENCODED_CHARS = new Funnel<>() {
            @Override
            public void funnel(CharSequence from, AbstractHasher into) {
                into.putUnencodedChars(from);
            }

            @Override
            public String toString() {
                return "Funnels.unencodedCharsFunnel()";
            }
        };
        BYTE_ARRAY = new Funnel<>() {
            @Override
            public void funnel(byte[] from, AbstractHasher into) {
                into.putBytes(from);
            }

            @Override
            public String toString() {
                return "Funnels.byteArrayFunnel()";
            }
        };
        INTEGER = new Funnel<>() {
            @Override
            public void funnel(Integer from, AbstractHasher into) {
                into.putInt(from);
            }

            @Override
            public String toString() {
                return "Funnels.integerFunnel()";
            }
        };
        LONG = new Funnel<>() {
            @Override
            public void funnel(Long from, AbstractHasher into) {
                into.putLong(from);
            }

            @Override
            public String toString() {
                return "Funnels.longFunnel()";
            }
        };
    }

}