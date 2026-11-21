package com.xxl.tool.datastructure.bloomfilter;

import com.xxl.tool.core.AssertTool;

import java.io.Serializable;
import static java.lang.Math.min;

/**
 * 哈希值抽象：封装定长字节序列，提供 {@code int}/{@code long}/{@code byte[]} 视图与相等性比较。
 *
 * <p>不可变且线程安全；位数必须是 8 的倍数。{@link #equals(Object)} 仅比较位数相同的哈希值，
 * 并对字节内容做常量时间比较，避免时序攻击。{@link #asInt()}、{@link #asLong()} 要求位数足够，
 * 不足时会抛出异常，具体类型由实现决定。
 *
 * @author xuxueli 2025-12-13
 */
public abstract class HashCode {

    // ---------------------- method ----------------------

    /**
     * 返回哈希码的位数，必须是 8 的倍数。
     *
     * @return 位数
     */
    public abstract int bits();

    /**
     * 将前 4 字节按小端序解释为 {@code int}。
     *
     * @return 32 位哈希值
     * @throws IllegalArgumentException 位数不足 32 位时抛出
     */
    public abstract int asInt();

    /**
     * 将前 8 字节按小端序解释为 {@code long}。
     *
     * @return 64 位哈希值
     * @throws RuntimeException 位数不足 64 位时抛出，具体类型由实现决定
     *                          （{@link IllegalStateException} 或 {@link IllegalArgumentException}）
     */
    public abstract long asLong();

    /**
     * 将哈希码安全转换为 {@code long}：最多取前 8 字节，位数不足时高位补 0。
     *
     * @return 转换后的长整数值
     */
    public abstract long padToLong();

    /**
     * 返回哈希码字节数组的副本，修改副本不影响当前实例。
     *
     * @return 字节数组副本
     */
    public abstract byte[] asBytes();

    /**
     * 返回内部字节数组视图，供内部高效访问；调用方不应修改返回数组。
     *
     * @return 内部字节数组，可能不是副本
     */
    public byte[] getBytesInternal() {
        return asBytes();
    }

    /**
     * 比较位数相同的两个哈希值的字节内容，由子类实现。
     *
     * @param that 待比较的哈希值，位数与当前实例相同
     * @return 字节内容相同返回 {@code true}
     */
    abstract boolean equalsSameBits(HashCode that);

    /**
     * 比较两个哈希值：位数相同且字节内容相同（常量时间比较）时相等。
     *
     * @param object 待比较对象
     * @return 相等返回 {@code true}，否则返回 {@code false}
     */
    @Override
    public final boolean equals(Object object) {
        if (object instanceof HashCode that) {
            return bits() == that.bits() && equalsSameBits(that);
        }
        return false;
    }

    /**
     * 返回适合放入哈希容器的哈希值：位数不少于 32 时取前 4 字节，否则拼接全部字节。
     *
     * @return 32 位哈希值
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
     * 返回小写十六进制字符串，每个字节固定两位。
     *
     * @return 十六进制字符串
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
     * 构造 32 位哈希值。
     *
     * @param hash 32 位哈希值
     * @return 哈希值实例
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
     * 构造 64 位哈希值。
     *
     * @param hash 64 位哈希值
     * @return 哈希值实例
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
     * 从字节数组创建哈希值，并复制数组以避免外部修改影响实例。
     *
     * @param bytes 字节数组，不能为 {@code null}；长度至少为 1
     * @return 哈希值实例
     * @throws IllegalArgumentException 数组长度为 0 时抛出
     */
    public static HashCode fromBytes(byte[] bytes) {
        AssertTool.isTrue(bytes.length >= 1, "A HashCode must contain at least 1 byte.");
        return fromBytesNoCopy(bytes.clone());
    }

    /**
     * 从字节数组创建哈希值，不复制数组；调用方后续修改数组会影响该实例。
     *
     * @param bytes 字节数组，不能为 {@code null}
     * @return 哈希值实例
     * @throws IllegalArgumentException 数组为 {@code null} 时抛出
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
