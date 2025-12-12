package com.xxl.tool.datastructure;

import com.xxl.tool.core.AssertTool;
import com.xxl.tool.datastructure.bloomfilter.Funnels;
import com.xxl.tool.datastructure.bloomfilter.HashStrategies;
import com.xxl.tool.datastructure.bloomfilter.LockFreeBitArray;

import java.io.Serializable;
import java.util.Objects;

import static java.lang.Double.MAX_EXPONENT;
import static java.lang.Math.*;

/**
 * bloom filter
 *
 * @author xuxueli 2025-12-13
 */
public final class BloomFilter<T extends Object> implements Serializable {
    private static final long serialVersionUID = 42L;

    // ---------------------- field ----------------------

    /**
     * bit array, store element data
     */
    private final LockFreeBitArray bitArray;

    /**
     * hash function count
     */
    private final int hashFunctionCount;

    /**
     * funnel, used to convert objects to bytes
     */
    private final Funnels.Funnel<? super T> funnel;

    /**
     * hash strategy
     */
    private final HashStrategies.Strategy strategy;

    private BloomFilter(
            LockFreeBitArray bitArray,
            int hashFunctionCount,
            Funnels.Funnel<? super T> funnel,
            HashStrategies.Strategy strategy) {

        AssertTool.isTrue(hashFunctionCount > 0, "hashFunctionCount (" + hashFunctionCount + ") must be > 0");
        AssertTool.isTrue(hashFunctionCount <= 255, "hashFunctionCount (" + hashFunctionCount + ") must be <= 255");
        AssertTool.notNull(bitArray, "bits must not be null");
        AssertTool.notNull(funnel, "funnel must not be null");
        AssertTool.notNull(strategy, "strategy must not be null");

        this.bitArray = bitArray;
        this.hashFunctionCount = hashFunctionCount;
        this.funnel = funnel;
        this.strategy = strategy;
    }


    // ---------------------- method ----------------------

    /**
     * check if object might be contained in bit array
     */
    public boolean mightContain(T object) {
        return strategy.mightContain(object, funnel, hashFunctionCount, bitArray);
    }

    /**
     * put object 2 bit array
     */
    public boolean put(T object) {
        return strategy.put(object, funnel, hashFunctionCount, bitArray);
    }

    /**
     * 计算误判率: 计算公式 =  (已设置位数/总位数) ^ 哈希函数个数
     */
    public double expectedFpp() {
        return Math.pow((double) bitArray.bitCount() / bitSize(), hashFunctionCount);
    }

    /**
     * 估算已插入元素数量：基于位设置比例的计算
     */
    public long approximateElementCount() {
        long bitSize = bitArray.bitSize();
        long bitCount = bitArray.bitCount();

        double fractionOfBitsSet = (double) bitCount / bitSize;
        return roundToLong(-Math.log1p(-fractionOfBitsSet) * bitSize / hashFunctionCount);
    }

    /**
     * 返回底层位数组的位数, 即布隆过滤器容量
     */
    public long bitSize() {
        return bitArray.bitSize();
    }

    /**
     * judge whether two BloomFilters can be merged safely
     */
    public boolean isCompatible(BloomFilter<T> that) {
        AssertTool.notNull(this, "BloomFilter must not be null");
        return this != that
                && this.hashFunctionCount == that.hashFunctionCount
                && this.bitSize() == that.bitSize()
                && this.strategy.equals(that.strategy)
                && this.funnel.equals(that.funnel);
    }

    /**
     * merge another BloomFilter to current instance
     * 将另一个过滤器的数据合并到当前实例（位或操作）
     */
    public void putAll(BloomFilter<T> that) {
        AssertTool.notNull(that,  "BloomFilter must not be null");
        AssertTool.isTrue(this != that, "Cannot combine a BloomFilter with itself.");
        AssertTool.isTrue(this.hashFunctionCount == that.hashFunctionCount, "BloomFilters must have the same number of hash functions ("+ this.hashFunctionCount +" != "+ that.hashFunctionCount +")");
        AssertTool.isTrue(this.bitSize() == that.bitSize(), "BloomFilters must have the same size underlying bit arrays ("+ this.bitSize() +" != "+ that.bitSize() +")");
        AssertTool.isTrue(this.strategy.equals(that.strategy), "BloomFilters must have equal strategies ("+ this.strategy +" != "+ that.strategy +")");
        AssertTool.isTrue(this.funnel.equals(that.funnel), "BloomFilters must have equal funnels ("+ this.funnel +" != "+ that.funnel +")");

        this.bitArray.putAll(that.bitArray);
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof BloomFilter<?> that) {
            return this.hashFunctionCount == that.hashFunctionCount
                    && this.funnel.equals(that.funnel)
                    && this.bitArray.equals(that.bitArray)
                    && this.strategy.equals(that.strategy);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(hashFunctionCount, funnel, strategy, bitArray);
    }


    // ---------------------- create ----------------------

    /**
     * create BloomFilter
     */
    public static BloomFilter<String> create(long size) {
        return create(Funnels.STRING, size, 0.03); // FYI, for 3%, we always get 5 hash functions
    }

    /**
     * create BloomFilter
     */
    public static BloomFilter<String> create(long expectedInsertions, double fpp) {
        return create(Funnels.STRING, expectedInsertions, fpp, HashStrategies.MURMURHASH3_128_MITZ_64);
    }

    /**
     * create BloomFilter
     */
    public static <T extends Object> BloomFilter<T> create(Funnels.Funnel<? super T> funnel, long expectedInsertions) {
        return create(funnel, expectedInsertions, 0.03); // FYI, for 3%, we always get 5 hash functions
    }

    /**
     * create BloomFilter
     */
    public static <T extends Object> BloomFilter<T> create(Funnels.Funnel<? super T> funnel, long size, double fpp) {
        return create(funnel, size, fpp, HashStrategies.MURMURHASH3_128_MITZ_64);
    }

    /**
     * create BloomFilter
     *
     * @param funnel                the funnel to convert objects to bytes
     * @param size                  the number of insertions to make
     * @param fpp                   the false-positive probability
     * @param strategy              the hash strategy to use
     * @return BloomFilter
     * @param <T> the type of elements to be stored in the BloomFilter
     */
    public static <T extends Object> BloomFilter<T> create(Funnels.Funnel<? super T> funnel,
                                                           long size,
                                                           double fpp,
                                                           HashStrategies.Strategy strategy) {

        AssertTool.notNull(funnel, "funnel must not be null");
        AssertTool.isTrue(size >= 0, "Expected insertions ("+size+") must be >= 0");
        AssertTool.isTrue(fpp > 0.0, "False positive probability ("+fpp+") must be > 0.0");
        AssertTool.isTrue(fpp < 1.0, "False positive probability ("+fpp+") must be < 1.0");
        AssertTool.notNull(strategy, "strategy must not be null");

        if (size <= 0) {
            size = 1;
        }

        // optimal bit-count and hash-function-count
        long bitCount = optimalNumOfBits(size, fpp);
        int hashFunctionCount = optimalNumOfHashFunctions(fpp);

        // build BloomFilter
        try {
            return new BloomFilter<>(new LockFreeBitArray(bitCount), hashFunctionCount, funnel, strategy);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Could not create BloomFilter of " + bitCount + " bits", e);
        }
    }


    // ---------------------- util (support) ----------------------

    /**
     * Pre-calculated value of natural-logarithm ln(2),  for optimizing BloomFilter capacity-calculation
     */
    private static final double LOG_TWO = Math.log(2);

    /**
     * Pre-calculated value of square of the natural-logarithm ln(2), used to optimize BloomFilter bitArray size-calculations
     */
    private static final double SQUARED_LOG_TWO = LOG_TWO * LOG_TWO;

    /**
     * 计算最优哈希函数数量
     *
     *  1、公式：k = -ln(p) / ln(2)，其中 p 是目标误判率
     *
     * @param p 目标误判率
     * @return 哈希函数数量
     */
    private static int optimalNumOfHashFunctions(double p) {
        // -log(p) / log(2), ensuring the result is rounded to avoid truncation.
        return max(1, (int) Math.round(-Math.log(p) / LOG_TWO));
    }

    /**
     * 计算最优比特数组大小
     *
     *  1、公式：m = -n * ln(p) / (ln(2))²，其中 n 是预期插入数量
     *
     * @param n expected insertions (must be positive)  插入数量
     * @param p false positive rate (must be 0 < p < 1) 误判率
     */
    private static long optimalNumOfBits(long n, double p) {
        if (p == 0) {
            p = Double.MIN_VALUE;
        }
        return (long) (-n * Math.log(p) / SQUARED_LOG_TWO);
    }


    // ---------------------- DoubleMath (support) ----------------------

    // the min and max values for long
    private static final double MIN_LONG_AS_DOUBLE = -0x1p63;
    private static final double MAX_LONG_AS_DOUBLE_PLUS_ONE = 0x1p63;

    /**
     * double 值四舍五入，并转换为 long
     */
    public static long roundToLong(double x) {
        double z = roundIntermediate(x);
        AssertTool.isTrue(MIN_LONG_AS_DOUBLE - z < 1.0 & z < MAX_LONG_AS_DOUBLE_PLUS_ONE, "rounded value is out of range for input " + x);
        return (long) z;
    }

    /**
     * 对 double 进行四舍五入处理
     */
    private static double roundIntermediate(double x) {
        if (!isFinite(x)) {
            throw new ArithmeticException("input is infinite or NaN");
        }
        // 使用标准的四舍五入
        double z = rint(x);
        // 处理正好处于中间值的情况（如 0.5, -0.5）
        if (abs(x - z) == 0.5) {
            return x + copySign(0.5, x);
        } else {
            return z;
        }
    }

    /**
     * 是否为有限数, 非 NaN 或无穷大
     */
    private static boolean isFinite(double d) {
        return getExponent(d) <= MAX_EXPONENT;
    }

}