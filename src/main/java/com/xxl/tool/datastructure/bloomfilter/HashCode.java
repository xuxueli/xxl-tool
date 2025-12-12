package com.xxl.tool.datastructure.bloomfilter;

import com.xxl.tool.core.AssertTool;

import java.io.Serializable;
import static java.lang.Math.min;

/**
 * hash code
 *
 * @author xuxueli 2025-12-13
 */
public abstract class HashCode {

    // ---------------------- method ----------------------

    /**
     * the number of bits in the hash code.
     * 返回哈希码的位数（必须是8的倍数）
     */
    public abstract int bits();

    /**
     * 将哈希码前4字节转换为int（小端序）
     */
    public abstract int asInt();

    /**
     * 将哈希码前8字节转换为long（小端序）
     */
    public abstract long asLong();

    /**
     * 安全地将哈希码转换为long，位数不足时用0填充
     */
    public abstract long padToLong();

    /**
     * 返回哈希码的字节数组表示
     */
    public abstract byte[] asBytes();

    /**
     * 返回哈希码的内部字节数组视图
     */
    public byte[] getBytesInternal() {
        return asBytes();
    }

    /**
     * 比较两个位数相同的哈希码是否相等
     */
    abstract boolean equalsSameBits(HashCode that);

    /**
     * 使用常量时间比较来防止时序攻击，先比较位数是否相同，再调用 equalsSameBits 比较具体字节内容。
     */
    @Override
    public final boolean equals(Object object) {
        if (object instanceof HashCode that) {
            return bits() == that.bits() && equalsSameBits(that);
        }
        return false;
    }

    /**
     * 为哈希容器（如 HashSet）提供兼容的哈希值。优先使用前4字节（32位），不足时拼接所有字节。
     */
    @Override
    public final int hashCode() {
        // If we have at least 4 bytes (32 bits), just take the first 4 bytes. Since this is
        // already a (presumably) high-quality hash code, any four bytes of it will do.
        if (bits() >= 32) {
            return asInt();
        }
        // If we have less than 4 bytes, use them all.
        byte[] bytes = getBytesInternal();
        int val = bytes[0] & 0xFF;
        for (int i = 1; i < bytes.length; i++) {
            val |= (bytes[i] & 0xFF) << (i * 8);
        }
        return val;
    }

    /**
     * 将字节转换为小写十六进制字符串，每个字节用两个十六进制数字表示。
     */
    @Override
    public final String toString() {
        byte[] bytes = getBytesInternal();
        StringBuilder sb = new StringBuilder(2 * bytes.length);
        for (byte b : bytes) {
            sb.append(hexDigits[(b >> 4) & 0xf]).append(hexDigits[b & 0xf]);
        }
        return sb.toString();
    }

    private static final char[] hexDigits = "0123456789abcdef".toCharArray();


    // ---------------------- IntHashCode (32位) ----------------------

    /**
     * 专门处理32位整数哈希
     */
    public static HashCode fromInt(int hash) {
        return new IntHashCode(hash);
    }

    private static final class IntHashCode extends HashCode implements Serializable {
        private static final long serialVersionUID = 0;

        final int hash;
        IntHashCode(int hash) {
            this.hash = hash;
        }

        @Override
        public int bits() {
            return 32;
        }

        @Override
        public byte[] asBytes() {
            return new byte[]{(byte) hash, (byte) (hash >> 8), (byte) (hash >> 16), (byte) (hash >> 24)};
        }

        @Override
        public int asInt() {
            return hash;
        }

        @Override
        public long asLong() {
            throw new IllegalStateException("this HashCode only has 32 bits; cannot create a long");
        }

        static final long INT_MASK = 0xffffffffL;
        @Override
        public long padToLong() {
            return hash & INT_MASK;
        }

        @Override
        boolean equalsSameBits(HashCode that) {
            return hash == that.asInt();
        }

    }


    // ---------------------- LongHashCode (64位) ----------------------

    /**
     * 处理64位长整数哈希
     */
    public static HashCode fromLong(long hash) {
        return new LongHashCode(hash);
    }

    private static final class LongHashCode extends HashCode implements Serializable {
        private static final long serialVersionUID = 0;

        final long hash;
        LongHashCode(long hash) {
            this.hash = hash;
        }

        @Override
        public int bits() {
            return 64;
        }

        @Override
        public byte[] asBytes() {
            return new byte[]{
                    (byte) hash,
                    (byte) (hash >> 8),
                    (byte) (hash >> 16),
                    (byte) (hash >> 24),
                    (byte) (hash >> 32),
                    (byte) (hash >> 40),
                    (byte) (hash >> 48),
                    (byte) (hash >> 56)
            };
        }

        @Override
        public int asInt() {
            return (int) hash;
        }

        @Override
        public long asLong() {
            return hash;
        }

        @Override
        public long padToLong() {
            return hash;
        }

        @Override
        boolean equalsSameBits(HashCode that) {
            return hash == that.asLong();
        }

    }


    // ---------------------- BytesHashCode (变长) ----------------------

    /**
     * 从字节数组创建HashCode实例
     */
    public static HashCode fromBytes(byte[] bytes) {
        AssertTool.isTrue(bytes.length >= 1, "A HashCode must contain at least 1 byte.");
        return fromBytesNoCopy(bytes.clone());
    }

    /**
     * 从字节数组创建HashCode实例，不进行数组复制
     */
    public static HashCode fromBytesNoCopy(byte[] bytes) {
        return new BytesHashCode(bytes);
    }

    private static final class BytesHashCode extends HashCode implements Serializable {
        private static final long serialVersionUID = 0;

        final byte[] bytes;
        BytesHashCode(byte[] bytes) {
            AssertTool.notNull(bytes, "bytes must not be null");
            this.bytes = bytes;
        }

        @Override
        public int bits() {
            return bytes.length * 8;
        }

        @Override
        public byte[] asBytes() {
            return bytes.clone();
        }

        @Override
        public int asInt() {
            AssertTool.isTrue(bytes.length >= 4, "HashCode#asInt() requires >= 4 bytes (it only has "+bytes.length+" bytes).");
            return (bytes[0] & 0xFF)
                    | ((bytes[1] & 0xFF) << 8)
                    | ((bytes[2] & 0xFF) << 16)
                    | ((bytes[3] & 0xFF) << 24);
        }

        @Override
        public long asLong() {
            AssertTool.isTrue(bytes.length >= 8, "HashCode#asLong() requires >= 8 bytes (it only has "+bytes.length+" bytes).");
            return padToLong();
        }

        @Override
        public long padToLong() {
            long retVal = bytes[0] & 0xFF;
            for (int i = 1; i < min(bytes.length, 8); i++) {
                retVal |= (bytes[i] & 0xFFL) << (i * 8);
            }
            return retVal;
        }

        @Override
        public byte[] getBytesInternal() {
            return bytes;
        }

        @Override
        boolean equalsSameBits(HashCode that) {
            if (this.bytes.length != that.getBytesInternal().length) {
                return false;
            }

            boolean areEqual = true;
            for (int i = 0; i < this.bytes.length; i++) {
                areEqual &= this.bytes[i] == that.getBytesInternal()[i];
            }
            return areEqual;
        }

    }

}
