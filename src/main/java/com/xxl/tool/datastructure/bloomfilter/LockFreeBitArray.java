package com.xxl.tool.datastructure.bloomfilter;

import com.xxl.tool.core.AssertTool;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.atomic.LongAdder;

/**
 * 无锁位数组：以 {@link AtomicLongArray} 存储位数据，通过 CAS 原子置位，适用于布隆过滤器的并发写入。
 *
 * <p>线程安全：{@link #get(long)}、{@link #set(long)}、{@link #putData(int, long)}、
 * {@link #putAll(LockFreeBitArray)} 均可被多个线程并发调用；位计数由 {@link LongAdder} 维护，
 * {@link #bitCount()} 是最终一致的近似快照。{@link #copy()} 返回独立快照。
 *
 * <p>内存与容量：每 64 位占用一个 {@code long}（8 字节），总内存约为 {@code bitSize() / 8} 字节；
 * 长度在构造时固定，不支持扩容。位下标从 0 开始，按 64 位分桶。
 *
 * @author xuxueli 2025-12-13
 */
public final class LockFreeBitArray {

    // 位数据，无锁存储；每个 long 保存 64 位
    final AtomicLongArray data;
    // 已置 1 的位数计数器
    private final LongAdder bitCount;

    /**
     * 按位数创建位数组；位数会向上取整为 64 的倍数，实际容量见 {@link #bitSize()}。
     *
     * @param bits 期望的位数，必须大于 0
     * @throws IllegalArgumentException 位数小于等于 0 时抛出
     */
    public LockFreeBitArray(long bits) {
        AssertTool.isTrue(bits > 0, "data length is zero!");

        long lengthL = longDivide(bits, 64);
        int length = (int) lengthL;
        AssertTool.isTrue(length == lengthL, "data length is not a multiple of 64 bits!");

        this.data = new AtomicLongArray(length);
        this.bitCount = new LongAdder();
    }

    /**
     * 从已有的 {@code long[]} 创建位数组，并统计其中已置 1 的位数。
     *
     * @param data 位数据数组，长度必须大于 0；数组后续修改不影响内部副本
     * @throws IllegalArgumentException 数组长度为 0 时抛出
     */
    public LockFreeBitArray(long[] data) {
        AssertTool.isTrue(data.length > 0, "data length is zero!");

        this.data = new AtomicLongArray(data);
        this.bitCount = new LongAdder();

        long bitCount = 0;
        for (long value : data) {
            bitCount += Long.bitCount(value);
        }
        this.bitCount.add(bitCount);
    }

    /**
     * 64位整数除法
     *
     * @param p 被除数
     * @param q 除数
     * @return 商（正数向上取整，负数向下取整）
     */
    private static long longDivide(long p, long q) {
        long div = p / q;       // throws if q == 0
        long rem = p - q * div; // equals p % q

        if (rem == 0) {
            return div;
        }

        int signum = 1 | (int) ((p ^ q) >> (Long.SIZE - 1));
        return signum > 0 ? div + signum : div;
    }

    private static final int LONG_ADDRESSABLE_BITS = 6;

    /**
     * 原子地把指定下标置 1。
     *
     * @param bitIndex 位下标，从 0 开始，必须小于 {@link #bitSize()}
     * @return 该位由 0 变为 1 返回 {@code true}；原本已为 1 返回 {@code false}
     * @throws ArrayIndexOutOfBoundsException 下标超出位数组范围时抛出
     */
    public boolean set(long bitIndex) {
        // 避免重复设置
        if (get(bitIndex)) {
            return false;
        }

        // 定位计算
        int longIndex = (int) (bitIndex >>> LONG_ADDRESSABLE_BITS);
        long mask = 1L << bitIndex; // only cares about low 6 bits of bitIndex

        // CAS 循环操作，无锁的原子操作
        long oldValue;
        long newValue;
        do {
            oldValue = data.get(longIndex);
            newValue = oldValue | mask;
            if (oldValue == newValue) {
                return false;
            }
        } while (!data.compareAndSet(longIndex, oldValue, newValue));

        // 计数器更新
        bitCount.increment();
        return true;
    }

    /**
     * 读取指定下标位的值。
     *
     * @param bitIndex 位下标，从 0 开始，必须小于 {@link #bitSize()}
     * @return 该位为 1 返回 {@code true}，否则返回 {@code false}
     * @throws ArrayIndexOutOfBoundsException 下标超出位数组范围时抛出
     */
    public boolean get(long bitIndex) {
        return (data.get((int) (bitIndex >>> LONG_ADDRESSABLE_BITS)) & (1L << bitIndex)) != 0;
    }

    /**
     * 把 {@link AtomicLongArray} 复制为普通 {@code long[]} 数组。
     *
     * @param atomicLongArray 源原子数组，不能为 {@code null}
     * @return 新的普通数组副本
     */
    public static long[] toPlainArray(AtomicLongArray atomicLongArray) {
        long[] array = new long[atomicLongArray.length()];
        for (int i = 0; i < array.length; ++i) {
            array[i] = atomicLongArray.get(i);
        }
        return array;
    }

    /**
     * 返回位数组总位数，即 {@code long} 数量乘以 64。
     *
     * @return 总位数，为 64 的倍数
     */
    public long bitSize() {
        return (long) data.length() * Long.SIZE;
    }

    /**
     * 返回已置 1 的位数。
     *
     * @return 已设置位数量；并发写入期间为近似值
     */
    public long bitCount() {
        return bitCount.sum();
    }

    /**
     * 复制当前位数组，返回内容相同但互不影响的新实例。
     *
     * @return 位数组副本
     */
    public LockFreeBitArray copy() {
        return new LockFreeBitArray(toPlainArray(data));
    }

    /**
     * 将另一个位数组按位或合并到当前实例。
     *
     * @param other 待合并的位数组，不能为 {@code null}，且长度必须与当前实例相同
     * @throws IllegalArgumentException 两个位数组长度不一致时抛出
     */
    public void putAll(LockFreeBitArray other) {
        AssertTool.isTrue(data.length() == other.data.length(), "BitArrays must be of equal length (" + data.length() + " != " + other.data.length() + ")");

        for (int i = 0; i < data.length(); i++) {
            putData(i, other.data.get(i));
        }
    }

    /**
     * 把指定 {@code long} 的掩码按位或到第 {@code i} 个桶，并更新已置位计数。
     *
     * @param i         数组索引，表示要操作的 long 元素位置
     * @param longValue 要合并的位掩码
     * @throws ArrayIndexOutOfBoundsException 索引超出底层数组范围时抛出
     */
    public void putData(int i, long longValue) {
        // CAS循环操作，直到更新成功或发现没有位变化为止
        long ourLongOld;
        long ourLongNew;
        boolean changedAnyBits = true;
        do {
            ourLongOld = data.get(i);
            ourLongNew = ourLongOld | longValue;
            if (ourLongOld == ourLongNew) {
                changedAnyBits = false;
                break;
            }
        } while (!data.compareAndSet(i, ourLongOld, ourLongNew));

        // 位计数更新
        if (changedAnyBits) {
            // 计算新旧值中1的位数差
            int bitsAdded = Long.bitCount(ourLongNew) - Long.bitCount(ourLongOld);
            bitCount.add(bitsAdded);
        }
    }

    /**
     * 返回底层 {@link AtomicLongArray} 的长度（long 桶数量，非位数）。
     *
     * @return long 桶数量
     */
    int dataLength() {
        return data.length();
    }

    /**
     * 比较两个位数组的位内容是否完全一致。
     *
     * @param o 待比较对象
     * @return 内容相同返回 {@code true}，否则返回 {@code false}
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof LockFreeBitArray lockFreeBitArray) {
            return Arrays.equals(toPlainArray(data), toPlainArray(lockFreeBitArray.data));
        }
        return false;
    }

    /**
     * 返回与位内容一致的哈希值。
     *
     * @return 哈希值
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(toPlainArray(data));
    }

}