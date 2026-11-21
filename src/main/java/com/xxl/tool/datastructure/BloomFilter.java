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
 * 布隆过滤器：基于位数组与多重哈希的概率型集合，用于判断元素「可能存在」或「一定不存在」。
 *
 * <p>特性与容量：
 * <ul>
 *   <li>不可删除：位一旦置 1 不会清除，删除元素只能重建过滤器；{@link #mightContain(Object)} 返回
 *       {@code false} 表示元素一定未插入，返回 {@code true} 仅表示可能插入（存在误判）。</li>
 *   <li>容量与误判率：按预期插入数与目标误判率（fpp）计算最优位数组大小和哈希函数数量；实际位数组位数会
 *       向上取整为 64 的倍数，{@link #bitSize()} 返回实际位数。插入量超过预期后误判率会上升。</li>
 *   <li>内存：位数组按 64 位 long 分配，约占 {@code bitSize() / 8} 字节，另有固定对象开销；容量在创建时
 *       确定，不会动态扩容。</li>
 *   <li>并发：底层为无锁位数组，{@link #put(Object)} 与 {@link #mightContain(Object)} 可被多线程并发调用；
 *       {@link #putAll(BloomFilter)} 与写入并发时结果不做强一致保证。</li>
 * </ul>
 *
 * @param <T> 元素类型
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
     * 判断元素是否可能已插入（存在误判，不会漏判）。
     *
     * @param object 待判断元素，不能为 {@code null}
     * @return 可能存在返回 {@code true}；一定不存在返回 {@code false}
     */
    public boolean mightContain(T object) {
        return strategy.mightContain(object, funnel, hashFunctionCount, bitArray);
    }

    /**
     * 将元素写入过滤器。
     *
     * @param object 待插入元素，不能为 {@code null}
     * @return 本次写入至少改变了一个位时返回 {@code true}；元素已存在（所有对应位已为 1）返回 {@code false}
     */
    public boolean put(T object) {
        return strategy.put(object, funnel, hashFunctionCount, bitArray);
    }

    /**
     * 估算当前误判率：公式为 {@code (已设置位数 / 总位数) ^ 哈希函数个数}。
     *
     * @return 当前误判率估算值，范围 {@code [0, 1]}
     */
    public double expectedFpp() {
        return Math.pow((double) bitArray.bitCount() / bitSize(), hashFunctionCount);
    }

    /**
     * 基于位设置比例估算已插入元素数量。
     *
     * @return 估算的元素数量；位数组为空时返回 0
     */
    public long approximateElementCount() {
        long bitSize = bitArray.bitSize();
        long bitCount = bitArray.bitCount();

        double fractionOfBitsSet = (double) bitCount / bitSize;
        return roundToLong(-Math.log1p(-fractionOfBitsSet) * bitSize / hashFunctionCount);
    }

    /**
     * 返回底层位数组的实际位数（创建时已向上取整为 64 的倍数）。
     *
     * @return 位数组位数
     */
    public long bitSize() {
        return bitArray.bitSize();
    }

    /**
     * 判断两个过滤器能否安全合并：哈希函数数量、位数组长度、哈希策略与 Funnel 均相同。
     *
     * @param that 另一个过滤器，不能为 {@code null}
     * @return 可安全合并返回 {@code true}；与当前实例相同、或任一配置不一致返回 {@code false}
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
     * 将另一个过滤器的位数据按位或合并到当前实例。
     *
     * @param that 待合并的过滤器，不能为 {@code null}，且必须与当前实例兼容
     * @throws IllegalArgumentException 与自身合并，或哈希函数数量、位数组长度、策略、Funnel 不一致时抛出
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

    /**
     * 比较两个过滤器是否等价：哈希函数数量、Funnel、位数组内容与哈希策略均相同。
     *
     * @param object 待比较对象
     * @return 等价返回 {@code true}，否则返回 {@code false}
     */
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

    /**
     * 返回与 {@link #equals(Object)} 一致的哈希值。
     *
     * @return 哈希值
     */
    @Override
    public int hashCode() {
        return Objects.hash(hashFunctionCount, funnel, strategy, bitArray);
    }


    // ---------------------- create ----------------------

    /**
     * 使用字符串 Funnel 与默认误判率 3% 创建过滤器。
     *
     * @param size 预期插入元素数量；为 0 时按 1 处理，负数会抛出异常
     * @return 字符串类型过滤器
     * @throws IllegalArgumentException 预期数量为负数时抛出
     */
    public static BloomFilter<String> create(long size) {
        return create(Funnels.STRING, size, 0.03); // FYI, for 3%, we always get 5 hash functions
    }

    /**
     * 使用字符串 Funnel 创建过滤器，并指定误判率。
     *
     * @param expectedInsertions 预期插入元素数量；为 0 时按 1 处理，负数会抛出异常
     * @param fpp                目标误判率，范围 {@code (0, 1)}
     * @return 字符串类型过滤器
     * @throws IllegalArgumentException 预期数量为负数或误判率越界时抛出
     */
    public static BloomFilter<String> create(long expectedInsertions, double fpp) {
        return create(Funnels.STRING, expectedInsertions, fpp, HashStrategies.MURMURHASH3_128_MITZ_64);
    }

    /**
     * 使用指定 Funnel 与默认误判率 3% 创建过滤器。
     *
     * @param funnel            元素到字节的转换器，不能为 {@code null}
     * @param expectedInsertions 预期插入元素数量；为 0 时按 1 处理，负数会抛出异常
     * @param <T>               元素类型
     * @return 指定元素类型的过滤器
     * @throws IllegalArgumentException Funnel 为 {@code null} 或预期数量为负数时抛出
     */
    public static <T extends Object> BloomFilter<T> create(Funnels.Funnel<? super T> funnel, long expectedInsertions) {
        return create(funnel, expectedInsertions, 0.03); // FYI, for 3%, we always get 5 hash functions
    }

    /**
     * 使用指定 Funnel 与误判率创建过滤器，哈希策略使用 MurmurHash3-128（64 位）。
     *
     * @param funnel 元素到字节的转换器，不能为 {@code null}
     * @param size   预期插入元素数量；为 0 时按 1 处理，负数会抛出异常
     * @param fpp    目标误判率，范围 {@code (0, 1)}
     * @param <T>    元素类型
     * @return 指定元素类型的过滤器
     * @throws IllegalArgumentException Funnel 为 {@code null}、预期数量为负数或误判率越界时抛出
     */
    public static <T extends Object> BloomFilter<T> create(Funnels.Funnel<? super T> funnel, long size, double fpp) {
        return create(funnel, size, fpp, HashStrategies.MURMURHASH3_128_MITZ_64);
    }

    /**
     * 完整创建方法：按预期插入数与目标误判率计算最优位数组大小和哈希函数数量。
     *
     * <p>位数组大小会向上取整为 64 的倍数；预期插入数为 0 时按 1 处理。
     *
     * @param funnel   元素到字节的转换器，不能为 {@code null}
     * @param size     预期插入元素数量；为 0 时按 1 处理，负数会抛出异常
     * @param fpp      目标误判率，范围 {@code (0, 1)}
     * @param strategy 多重哈希策略，不能为 {@code null}
     * @param <T>      元素类型
     * @return 指定元素类型的过滤器
     * @throws IllegalArgumentException 参数非法，或位数组过大无法分配时抛出
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
     * 对 {@code double} 四舍五入并转换为 {@code long}；正好处于中间值时向远离 0 的方向取整。
     *
     * @param x 待取整的数值，必须为有限数
     * @return 四舍五入后的长整数值
     * @throws ArithmeticException 输入为 NaN 或无穷大时抛出
     * @throws IllegalArgumentException 取整结果超出 {@code long} 范围时抛出
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