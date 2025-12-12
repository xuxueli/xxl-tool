package com.xxl.tool.datastructure.bloomfilter;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Funnels : parse object to byte array, with Hasher
 *
 * @author xuxueli 2025-12-13
 */
public class Funnels {

    // ---------------------- funnel iface ----------------------

    public interface Funnel<T extends Object> extends Serializable {

        /**
         * convert object to byte array
         *
         * @param from source object, provides data to be converted
         * @param into target receiver, used to receive data
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