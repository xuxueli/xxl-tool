package com.xxl.tool.datastructure.bloomfilter;

import com.xxl.tool.core.AssertTool;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.atomic.LongAdder;

/**
 * lock-free bit array
 *
 * @author xuxueli 2025-12-13
 */
public final class LockFreeBitArray {

    // used to store the bits, without locks; each long stores 64 bits
    final AtomicLongArray data;
    // used to count the number of bits set
    private final LongAdder bitCount;

    public LockFreeBitArray(long bits) {
        AssertTool.isTrue(bits > 0, "data length is zero!");

        long lengthL = longDivide(bits, 64);
        int length = (int) lengthL;
        AssertTool.isTrue(length == lengthL, "data length is not a multiple of 64 bits!");

        this.data = new AtomicLongArray(length);
        this.bitCount = new LongAdder();
    }

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
     * 设置指定位为1
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
     * 读取指定位的值
     */
    public boolean get(long bitIndex) {
        return (data.get((int) (bitIndex >>> LONG_ADDRESSABLE_BITS)) & (1L << bitIndex)) != 0;
    }

    /**
     * 将线程安全的 AtomicLongArray 转换为普通的 long[] 数组
     */
    public static long[] toPlainArray(AtomicLongArray atomicLongArray) {
        long[] array = new long[atomicLongArray.length()];
        for (int i = 0; i < array.length; ++i) {
            array[i] = atomicLongArray.get(i);
        }
        return array;
    }

    /**
     * the length of the BitArray
     * 位数组的总长度，每个long类型64位
     */
    public long bitSize() {
        return (long) data.length() * Long.SIZE;
    }

    /**
     * Number of bits (bits with value 1) already set
     * 已设置位（值为1的位）的数量
     */
    public long bitCount() {
        return bitCount.sum();
    }

    /**
     * copy current BitArray
     */
    public LockFreeBitArray copy() {
        return new LockFreeBitArray(toPlainArray(data));
    }

    /**
     * 将另一个过滤器的数据合并到当前实例（位或操作）
     */
    public void putAll(LockFreeBitArray other) {
        AssertTool.isTrue(data.length() == other.data.length(), "BitArrays must be of equal length (" + data.length() + " != " + other.data.length() + ")");

        for (int i = 0; i < data.length(); i++) {
            putData(i, other.data.get(i));
        }
    }

    /**
     * 设置位并统计设置位的数量
     *
     * @param i         数组索引，表示要操作的long元素位置
     * @param longValue 要设置的位掩码，值为2的幂次方或多个2的幂次方的组合
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
     * 底层AtomicLongArray的长度
     */
    int dataLength() {
        return data.length();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof LockFreeBitArray lockFreeBitArray) {
            return Arrays.equals(toPlainArray(data), toPlainArray(lockFreeBitArray.data));
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(toPlainArray(data));
    }

}