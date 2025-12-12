package com.xxl.tool.datastructure.bloomfilter;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/*
 * abstract hasher
 *
 *  1、支持基本数据类型（byte、short、int、long等）的哈希处理
 *  2、支持字符串和字符序列的处理（支持未编码字符和指定字符集编码）
 *  3、支持通过Funnel接口处理任意对象
 *  4、支持通过Funnel接口处理任意对象
 *
 * @author xuxueli 2025-12-13
 */
public abstract class AbstractHasher {

    /**
     * put byte
     */
    public abstract AbstractHasher putByte(byte b);

    /**
     * put bytes
     */
    public AbstractHasher putBytes(byte[] bytes) {
        return putBytes(bytes, 0, bytes.length);
    }

    /**
     * put bytes with offset and length
     */
    public AbstractHasher putBytes(byte[] bytes, int off, int len) {
        checkPositionIndexes(off, off + len, bytes.length);
        for (int i = 0; i < len; i++) {
            putByte(bytes[off + i]);
        }
        return this;
    }

    /**
     * 检查数组索引范围的合法性
     */
    private static void checkPositionIndexes(int start, int end, int size) {
        // Carefully optimized for execution by hotspot (explanatory comment above)
        if (start < 0 || end < start || end > size) {
            throw new IndexOutOfBoundsException("start: " + start + ", end: " + end + ", size: " + size);
        }
    }

    /**
     * put byte buffer
     */
    public AbstractHasher putBytes(ByteBuffer b) {
        if (b.hasArray()) {
            // 当ByteBuffer有底层数组时，直接调用putBytes方法批量处理数组数据，然后将位置设为限制位置
            putBytes(b.array(), b.arrayOffset() + b.position(), b.remaining());
            b.position(b.limit());
        } else {
            // 当ByteBuffer没有底层数组时（如直接内存），逐个读取字节并通过putByte方法添加
            for (int remaining = b.remaining(); remaining > 0; remaining--) {
                putByte(b.get());
            }
        }
        return this;
    }

    /**
     * put short
     */
    public AbstractHasher putShort(short s) {
        putByte((byte) s);
        putByte((byte) (s >>> 8));
        return this;
    }

    /**
     * put int
     */
    public AbstractHasher putInt(int i) {
        putByte((byte) i);
        putByte((byte) (i >>> 8));
        putByte((byte) (i >>> 16));
        putByte((byte) (i >>> 24));
        return this;
    }

    /**
     * put long
     */
    public AbstractHasher putLong(long l) {
        for (int i = 0; i < 64; i += 8) {
            putByte((byte) (l >>> i));
        }
        return this;
    }

    /**
     * put float
     *
     * Equivalent to {@code putInt(Float.floatToRawIntBits(f))}.
     */
    public final AbstractHasher putFloat(float f) {
        return putInt(Float.floatToRawIntBits(f));
    }

    /**
     * put double
     *
     * Equivalent to {@code putLong(Double.doubleToRawLongBits(d))}.
     */
    public final AbstractHasher putDouble(double d) {
        return putLong(Double.doubleToRawLongBits(d));
    }

    /**
     * put boolean
     *
     * Equivalent to {@code putByte(b ? (byte) 1 : (byte) 0)}.
     */
    public final AbstractHasher putBoolean(boolean b) {
        return putByte(b ? (byte) 1 : (byte) 0);
    }

    /**
     * put char
     */
    public AbstractHasher putChar(char c) {
        putByte((byte) c);
        putByte((byte) (c >>> 8));
        return this;
    }

    /**
     * put char sequence
     *
     * Equivalent to processing each {@code char} value in the {@code CharSequence}, in order. In
     * other words, no character encoding is performed; the low byte and high byte of each {@code
     * char} are hashed directly (in that order). The input must not be updated while this method is
     * in progress.
     */
    public AbstractHasher putUnencodedChars(CharSequence charSequence) {
        for (int i = 0, len = charSequence.length(); i < len; i++) {
            putChar(charSequence.charAt(i));
        }
        return this;
    }

    /**
     * put string
     *
     * Equivalent to {@code putBytes(charSequence.toString().getBytes(charset))}.
     */
    public AbstractHasher putString(CharSequence charSequence, Charset charset) {
        return putBytes(charSequence.toString().getBytes(charset));
    }

    /**
     * put object
     *
     * A simple convenience for {@code funnel.funnel(object, this)}.
     */
    public <T extends Object> AbstractHasher putObject(T instance, Funnels.Funnel<? super T> funnel) {
        funnel.funnel(instance, this);
        return this;
    }

    /**
     * get HashCode object
     *
     * Computes a hash code based on the data that have been provided to this hasher. The result is
     * unspecified if this method is called more than once on the same instance.
     */
    public abstract HashCode hash();


    /**
     * get hash code
     *
     * This returns {@link Object#hashCode()}; you almost certainly mean to call {@code hash().asInt()}.
     */
    //public abstract int hashCode();

}