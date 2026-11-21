package com.xxl.tool.datastructure.bloomfilter;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * 哈希计算器抽象基类：接收基本类型、字节、字符序列与任意对象，由子类实现具体哈希算法。
 *
 * <p>支持范围：
 * <ol>
 *   <li>基本类型：byte、short、int、long、float、double、boolean、char，按小端序写入；</li>
 *   <li>字节数组与 {@link ByteBuffer}；</li>
 *   <li>字符串与字符序列：支持未编码字符（直接写入 char 的低/高字节）和指定字符集编码；</li>
 *   <li>任意对象：通过 {@link Funnels.Funnel} 转换为字节后写入。</li>
 * </ol>
 *
 * <p>实例有状态且非线程安全；通常一次性使用，调用 {@link #hash()} 后不应继续写入，否则结果未定义。
 *
 * @author xuxueli 2025-12-13
 */
public abstract class AbstractHasher {

    /**
     * 写入一个字节。
     *
     * @param b 待写入字节
     * @return 当前 hasher，便于链式调用
     */
    public abstract AbstractHasher putByte(byte b);

    /**
     * 写入整个字节数组。
     *
     * @param bytes 待写入字节数组，不能为 {@code null}
     * @return 当前 hasher，便于链式调用
     */
    public AbstractHasher putBytes(byte[] bytes) {
        return putBytes(bytes, 0, bytes.length);
    }

    /**
     * 写入字节数组的指定区间 {@code [off, off + len)}。
     *
     * @param bytes 源字节数组，不能为 {@code null}
     * @param off   起始偏移，不能为负数
     * @param len   写入长度，不能为负数
     * @return 当前 hasher，便于链式调用
     * @throws IndexOutOfBoundsException 区间越界（{@code off < 0}、{@code len < 0} 或
     *                                   {@code off + len > bytes.length}）时抛出
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
     * 写入 {@link ByteBuffer} 的剩余内容，并把其 position 推进到 limit。
     *
     * @param b 源缓冲区，不能为 {@code null}
     * @return 当前 hasher，便于链式调用
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
     * 以小端序写入一个 {@code short}（低字节在前，共 2 字节）。
     *
     * @param s 待写入值
     * @return 当前 hasher，便于链式调用
     */
    public AbstractHasher putShort(short s) {
        putByte((byte) s);
        putByte((byte) (s >>> 8));
        return this;
    }

    /**
     * 以小端序写入一个 {@code int}（低字节在前，共 4 字节）。
     *
     * @param i 待写入值
     * @return 当前 hasher，便于链式调用
     */
    public AbstractHasher putInt(int i) {
        putByte((byte) i);
        putByte((byte) (i >>> 8));
        putByte((byte) (i >>> 16));
        putByte((byte) (i >>> 24));
        return this;
    }

    /**
     * 以小端序写入一个 {@code long}（低字节在前，共 8 字节）。
     *
     * @param l 待写入值
     * @return 当前 hasher，便于链式调用
     */
    public AbstractHasher putLong(long l) {
        for (int i = 0; i < 64; i += 8) {
            putByte((byte) (l >>> i));
        }
        return this;
    }

    /**
     * 写入一个 {@code float}：等价于 {@code putInt(Float.floatToRawIntBits(f))}。
     *
     * @param f 待写入值
     * @return 当前 hasher，便于链式调用
     */
    public final AbstractHasher putFloat(float f) {
        return putInt(Float.floatToRawIntBits(f));
    }

    /**
     * 写入一个 {@code double}：等价于 {@code putLong(Double.doubleToRawLongBits(d))}。
     *
     * @param d 待写入值
     * @return 当前 hasher，便于链式调用
     */
    public final AbstractHasher putDouble(double d) {
        return putLong(Double.doubleToRawLongBits(d));
    }

    /**
     * 写入一个 {@code boolean}：{@code true} 写 1，{@code false} 写 0。
     *
     * @param b 待写入值
     * @return 当前 hasher，便于链式调用
     */
    public final AbstractHasher putBoolean(boolean b) {
        return putByte(b ? (byte) 1 : (byte) 0);
    }

    /**
     * 写入一个 {@code char}：先写低字节、再写高字节（共 2 字节）。
     *
     * @param c 待写入字符
     * @return 当前 hasher，便于链式调用
     */
    public AbstractHasher putChar(char c) {
        putByte((byte) c);
        putByte((byte) (c >>> 8));
        return this;
    }

    /**
     * 逐个写入字符序列中的 {@code char}，不做字符编码：每个 char 直接写低字节和高字节。
     *
     * <p>写入过程中调用方不应修改该字符序列。
     *
     * @param charSequence 待写入字符序列，不能为 {@code null}
     * @return 当前 hasher，便于链式调用
     */
    public AbstractHasher putUnencodedChars(CharSequence charSequence) {
        for (int i = 0, len = charSequence.length(); i < len; i++) {
            putChar(charSequence.charAt(i));
        }
        return this;
    }

    /**
     * 按指定字符集编码后写入字符串：等价于 {@code putBytes(charSequence.toString().getBytes(charset))}。
     *
     * @param charSequence 待写入字符序列，不能为 {@code null}
     * @param charset      字符集，不能为 {@code null}
     * @return 当前 hasher，便于链式调用
     */
    public AbstractHasher putString(CharSequence charSequence, Charset charset) {
        return putBytes(charSequence.toString().getBytes(charset));
    }

    /**
     * 通过 Funnel 把对象转换为字节后写入：等价于 {@code funnel.funnel(instance, this)}。
     *
     * @param instance 待写入对象，不能为 {@code null}（是否允许 {@code null} 由 Funnel 决定）
     * @param funnel   对象转换器，不能为 {@code null}
     * @param <T>      对象类型
     * @return 当前 hasher，便于链式调用
     */
    public <T extends Object> AbstractHasher putObject(T instance, Funnels.Funnel<? super T> funnel) {
        funnel.funnel(instance, this);
        return this;
    }

    /**
     * 基于已写入的数据计算哈希值。
     *
     * <p>通常只应调用一次；对同一实例重复调用或调用后继续写入时，结果未定义（具体由子类决定）。
     *
     * @return 哈希值
     */
    public abstract HashCode hash();


    /**
     * 转换为 {@link Object#hashCode()} 的说明（并未覆盖该方法）。
     *
     * <p>此方法返回的是对象标识哈希；若要获取哈希算法结果，请调用 {@code hash().asInt()}。
     */
    //public abstract int hashCode();

}