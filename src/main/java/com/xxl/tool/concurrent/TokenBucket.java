package com.xxl.tool.concurrent;


import com.xxl.tool.core.AssertTool;

import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.min;
import static java.util.concurrent.TimeUnit.*;

/**
 * 令牌桶限流器：按固定速率发放许可，支持平滑突发（SmoothBursty）与预热（SmoothWarmingUp）两种模式。
 *
 * <p>速率、突发与预热：
 * <ul>
 *   <li>{@link #create(double)} 创建 {@link SmoothBursty} 模式，速率单位为「许可/秒」，桶容量为 1 秒的
 *       许可数（maxBurstSeconds=1）；空闲后最多可突发约 1 秒额度的请求。</li>
 *   <li>{@link #create(double, Duration)} 等重载创建 {@link SmoothWarmingUp} 模式：从冷启动速率逐步提升到
 *       稳定速率，预热期越长、冷却因子越大，初始阶段放行越慢；预热完成后行为接近平滑突发模式。</li>
 *   <li>{@link #acquire()} 阻塞到令牌可用，返回实际等待秒数；{@link #tryAcquire(long, TimeUnit)} 在超时
 *       预算内无法获得足够令牌时立即返回 {@code false}，不会阻塞等待。</li>
 * </ul>
 *
 * <p>线程安全：桶状态的读写都在内部 mutex 上同步，可被多个线程共享；等待在锁外进行，避免持锁休眠。
 * 时间基准为 {@link System#nanoTime()} 提供的单调时钟。许可数必须为正数、速率必须大于 0，
 * 非法参数抛出 {@link IllegalArgumentException}。
 *
 * @author xuxueli 2025-09-05
 */
public abstract class TokenBucket {

    // ---------------------- create ----------------------

    /**
     * 创建平滑突发（SmoothBursty）令牌桶，桶容量为 1 秒的许可数。
     *
     * @param permitsPerSecond 每秒许可数，必须大于 0
     * @return 令牌桶实例
     * @throws IllegalArgumentException 速率不是正数时抛出
     */
    public static TokenBucket create(double permitsPerSecond) {
        TokenBucket tokenBucket = new SmoothBursty(1.0);
        tokenBucket.setRate(permitsPerSecond);
        return tokenBucket;
    }

    /**
     * 创建预热（SmoothWarmingUp）令牌桶，冷却因子使用默认值 3.0。
     *
     * @param permitsPerSecond 每秒许可数，必须大于 0
     * @param warmupPeriod     预热周期，不能为负数且应大于 0；传 0 会导致内部冷却间隔退化
     * @return 令牌桶实例
     * @throws IllegalArgumentException 速率不是正数或预热周期为负数时抛出
     */
    public static TokenBucket create(double permitsPerSecond, Duration warmupPeriod) {
        return create(permitsPerSecond, toNanosSaturated(warmupPeriod), TimeUnit.NANOSECONDS);
    }

    /**
     * 创建预热（SmoothWarmingUp）令牌桶，冷却因子使用默认值 3.0。
     *
     * @param permitsPerSecond 每秒许可数，必须大于 0
     * @param warmupPeriod     预热周期，不能为负数且应大于 0；传 0 会导致内部冷却间隔退化
     * @param unit             预热周期的时间单位，不能为 {@code null}
     * @return 令牌桶实例
     * @throws IllegalArgumentException 速率不是正数或预热周期为负数时抛出
     */
    public static TokenBucket create(double permitsPerSecond, long warmupPeriod, TimeUnit unit) {
        AssertTool.isTrue(warmupPeriod >= 0, "warmupPeriod must not be negative: " + warmupPeriod);
        return create(permitsPerSecond, warmupPeriod, unit, 3.0);
    }

    /**
     * 创建预热（SmoothWarmingUp）令牌桶。
     *
     * @param permitsPerSecond 每秒许可数，必须大于 0
     * @param warmupPeriod     预热周期，不能为负数且应大于 0；传 0 会导致内部冷却间隔退化
     * @param unit             预热周期的时间单位，不能为 {@code null}
     * @param coldFactor       冷却因子，默认 3.0；值越大冷启动间隔越长、初始放行越慢，
     *                         冷启动间隔 = 冷却因子 × 稳定间隔
     * @return 令牌桶实例
     * @throws IllegalArgumentException 速率不是正数或预热周期为负数时抛出
     */
    public static TokenBucket create(double permitsPerSecond, long warmupPeriod, TimeUnit unit, double coldFactor) {
        TokenBucket tokenBucket = new SmoothWarmingUp(warmupPeriod, unit, coldFactor);
        tokenBucket.setRate(permitsPerSecond);
        return tokenBucket;
    }

    // ---------------------- param ----------------------

    /** 当前已存储的许可数 */
    protected double storedPermits;
    /** 存储许可数的上限，由速率与模式决定 */
    protected double maxPermits;
    /** 稳定状态下相邻许可的时间间隔，单位微秒 */
    protected double stableIntervalMicros;
    /** 当前配置的速率，单位许可/秒 */
    protected double permitsPerSecond;
    /** 下一个许可可用的时间点，单位微秒（相对 stopwatch 起点，可能在过去或未来） */
    private long nextFreeTicketMicros = 0L; // could be either in the past or future
    /** 单调计时器，提供微秒级时间基准 */
    private final Stopwatch stopwatch = Stopwatch.createStarted();
    /** 双重检查锁定的互斥对象，延迟创建以减少无竞争场景开销 */
    private volatile Object mutexDoNotUseDirectly;

    /**
     * 获取内部互斥对象，首次访问时通过双重检查锁定安全创建。
     *
     * @return 非空的互斥对象
     */
    private Object mutex() {
        Object mutex = mutexDoNotUseDirectly;
        if (mutex == null) {
            synchronized (this) {
                mutex = mutexDoNotUseDirectly;
                if (mutex == null) {
                    mutexDoNotUseDirectly = mutex = new Object();
                }
            }
        }
        return mutex;
    }

    // ---------------------- rate ----------------------

    /**
     * 设置令牌桶速率：先按当前时间补齐令牌，再更新稳定间隔并按模式重算容量与已存令牌。
     *
     * <p>该方法可在运行期间调用，调用后桶状态会尽量平滑过渡；线程安全。
     *
     * @param permitsPerSecond 每秒许可数，必须大于 0
     * @throws IllegalArgumentException 速率不是正数时抛出
     */
    public final void setRate(double permitsPerSecond) {
        AssertTool.isTrue(permitsPerSecond > 0.0, "rate must be positive");
        synchronized (mutex()) {
            // 重新同步令牌桶的状态
            resync(stopwatch.elapsed(TimeUnit.MICROSECONDS));

            this.permitsPerSecond = permitsPerSecond;
            this.stableIntervalMicros = SECONDS.toMicros(1L) / permitsPerSecond;
            doSetRate();
        }
    }

    /**
     * 按当前速率重新计算桶容量与已存令牌数，由 {@link #setRate(double)} 在持锁状态下调用。
     */
    abstract void doSetRate();

    /**
     * 按当前时间补充令牌：仅当 {@code nowMicros} 晚于下次可发放时间时计算新增许可，
     * 并把已存许可数截断到 {@link #maxPermits}。
     *
     * @param nowMicros 相对 stopwatch 起点的当前时间，单位微秒
     */
    void resync(long nowMicros) {
        // 检查时间差，是否刷新/生成令牌
        if (nowMicros > nextFreeTicketMicros) {
            // 计算待生成令牌，并刷新
            double newPermits = (nowMicros - nextFreeTicketMicros) / coolDownIntervalMicros();
            storedPermits = min(maxPermits, storedPermits + newPermits);
            // 刷新下次生成令牌时间
            nextFreeTicketMicros = nowMicros;
        }
    }

    // ---------------------- cool /wait ----------------------

    /**
     * 计算获取指定数量「已存许可」需要额外等待的时间，由具体模式实现。
     *
     * @param permitsToTake 要消耗的已存许可数
     * @return 需要等待的时长，单位微秒
     */
    abstract long storedPermitsToWaitTime(double permitsToTake);

    /**
     * 返回当前模式下生成一个令牌的平均冷却间隔，用于按时间补充许可。
     *
     * @return 冷却间隔，单位微秒
     */
    abstract double coolDownIntervalMicros();

    // ---------------------- acquire ----------------------

    /**
     * 阻塞获取 1 个许可。
     *
     * <p>若当前无可用许可，会按令牌桶速率休眠等待；等待期间不响应中断，但会恢复中断标记。
     *
     * @return 实际等待时长，单位秒；无需等待时返回 0
     */
    public double acquire() {
        return acquire(1);
    }

    /**
     * 阻塞获取指定数量的许可。
     *
     * <p>若当前令牌不足，会按令牌桶速率休眠等待；等待期间不响应中断，但会恢复中断标记。
     *
     * @param permits 请求的许可数，必须大于 0
     * @return 实际等待时长，单位秒；无需等待时返回 0
     * @throws IllegalArgumentException 许可数不是正数时抛出
     */
    public double acquire(int permits) {
        long microsToWait = reserve(permits);

        if (microsToWait > 0) {
            sleepUninterruptibly(microsToWait, TimeUnit.MICROSECONDS);
        }

        return 1.0 * microsToWait / TimeUnit.SECONDS.toMicros(1L);
    }

    /**
     * 预留指定数量的许可并返回需要等待的时长（不阻塞、不睡眠）。
     *
     * @param permits 请求的许可数，必须大于 0
     * @return 需要等待的时长，单位微秒，最小为 0
     * @throws IllegalArgumentException 许可数不是正数时抛出
     */
    final long reserve(int permits) {
        AssertTool.isTrue(permits > 0, "Requested permits must be positive, permits:" + permits);
        synchronized (mutex()) {
            return reserveAndGetWaitLength(permits, stopwatch.elapsed(TimeUnit.MICROSECONDS));
        }
    }

    // ---------------------- tryAcquire ----------------------

    /**
     * 尝试立即获取 1 个许可：仅当当前已有可用令牌时成功，不等待。
     *
     * @return 获取成功返回 {@code true}；需要等待时返回 {@code false}
     */
    public boolean tryAcquire() {
        return tryAcquire(1, 0, TimeUnit.MICROSECONDS);
    }

    /**
     * 尝试在超时预算内获取 1 个许可。
     *
     * @param timeout 最长等待时间，不能为 {@code null}
     * @return 在超时预算内获取成功返回 {@code true}，否则返回 {@code false}
     */
    public boolean tryAcquire(Duration timeout) {
        return tryAcquire(1, toNanosSaturated(timeout), TimeUnit.NANOSECONDS);
    }

    /**
     * 尝试在超时预算内获取 1 个许可。
     *
     * @param timeout 最长等待时间
     * @param unit    超时时间单位，不能为 {@code null}
     * @return 在超时预算内获取成功返回 {@code true}，否则返回 {@code false}
     */
    public boolean tryAcquire(long timeout, TimeUnit unit) {
        return tryAcquire(1, timeout, unit);
    }

    /**
     * 尝试立即获取指定数量的许可：仅当当前已有足够令牌时成功，不等待。
     *
     * @param permits 请求的许可数，必须大于 0
     * @return 获取成功返回 {@code true}；需要等待时返回 {@code false}
     * @throws IllegalArgumentException 许可数不是正数时抛出
     */
    public boolean tryAcquire(int permits) {
        return tryAcquire(permits, 0, TimeUnit.MICROSECONDS);
    }

    /**
     * 尝试在超时预算内获取指定数量的许可。
     *
     * @param permits 请求的许可数，必须大于 0
     * @param timeout 最长等待时间，不能为 {@code null}
     * @return 在超时预算内获取成功返回 {@code true}，否则返回 {@code false}
     * @throws IllegalArgumentException 许可数不是正数时抛出
     */
    public boolean tryAcquire(int permits, Duration timeout) {
        return tryAcquire(permits, toNanosSaturated(timeout), TimeUnit.NANOSECONDS);
    }

    /**
     * 尝试在超时预算内获取指定数量的许可。
     *
     * <p>先判断最早可用令牌时间是否落在超时预算内，若可满足则预留许可并按需休眠；否则立即返回
     * {@code false}，不产生等待。休眠期间不响应中断，但会恢复中断标记。
     *
     * @param permits 请求的许可数，必须大于 0
     * @param timeout 最长等待时间，负数按 0 处理
     * @param unit    超时时间单位，不能为 {@code null}
     * @return 在超时预算内获取成功返回 {@code true}，否则返回 {@code false}
     * @throws IllegalArgumentException 许可数不是正数时抛出
     */
    public boolean tryAcquire(int permits, long timeout, TimeUnit unit) {
        // valid
        long timeoutMicros = Math.max(unit.toMicros(timeout), 0);
        AssertTool.isTrue(permits > 0, "Requested permits must be positive, permits:" + permits);

        // acquire and calculate wait-time
        long microsToWait;
        synchronized (mutex()) {
            long nowMicros = stopwatch.elapsed(TimeUnit.MICROSECONDS);
            if (!canAcquire(nowMicros, timeoutMicros)) {
                return false;
            } else {
                microsToWait = reserveAndGetWaitLength(permits, nowMicros);
            }
        }

        // wait
        if (microsToWait > 0) {
            sleepUninterruptibly(microsToWait, TimeUnit.MICROSECONDS);
        }

        return true;
    }

    /**
     * 判断在给定超时预算内是否可能获得令牌（仅比较下次可用时间，不预留下许可）。
     *
     * @param nowMicros     相对 stopwatch 起点的当前时间，单位微秒
     * @param timeoutMicros 允许等待的时长，单位微秒
     * @return 最早可用时间不晚于 {@code nowMicros + timeoutMicros} 时返回 {@code true}
     */
    private boolean canAcquire(long nowMicros, long timeoutMicros) {
        return nextFreeTicketMicros <= nowMicros + timeoutMicros;
    }

    /**
     * 预留指定许可并返回需要等待的微秒数（不阻塞）。
     *
     * @param permits   请求的许可数
     * @param nowMicros 相对 stopwatch 起点的当前时间，单位微秒
     * @return 需要等待的时长，单位微秒，最小为 0
     */
    final long reserveAndGetWaitLength(int permits, long nowMicros) {
        long momentAvailable = reserveEarliestAvailable(permits, nowMicros);
        return Math.max(momentAvailable - nowMicros, 0);
    }

    /**
     * 预留最早可用的许可，并更新下一次可用时间与已存许可数。
     *
     * <p>优先消耗已存许可，不足部分按稳定间隔折算为等待时间；返回值为调用前的「下一次可用时间」，
     * 因此调用方需要等待的时长等于返回值减去 {@code nowMicros}（最小 0）。
     *
     * @param requiredPermits 请求的许可数
     * @param nowMicros       相对 stopwatch 起点的当前时间，单位微秒
     * @return 预留前的最早可用时间，单位微秒
     */
    final long reserveEarliestAvailable(int requiredPermits, long nowMicros) {
        // 同步令牌桶状态
        resync(nowMicros);
        // 标记 nextFreeTicketMicros 作为返回值
        long returnValue = nextFreeTicketMicros;
        // 计算可以使用的许可证数量
        double storedPermitsToSpend = min(requiredPermits, this.storedPermits);
        // 计算需要补充的许可证数量
        double freshPermits = requiredPermits - storedPermitsToSpend;
        // 计算等待时间
        long waitMicros = storedPermitsToWaitTime(storedPermitsToSpend) + (long) (freshPermits * stableIntervalMicros);

        // 更新下一个可用令牌的时间
        this.nextFreeTicketMicros = saturatedAdd(nextFreeTicketMicros, waitMicros);
        // 减少已存储的令牌数量
        this.storedPermits -= storedPermitsToSpend;
        return returnValue;
    }

    /**
     * 返回描述当前速率的字符串。
     *
     * @return 形如 {@code TokenBucket[stableRate=5.0qps]} 的字符串
     */
    @Override
    public String toString() {
        return String.format(Locale.ROOT, "TokenBucket[stableRate=%3.1fqps]", permitsPerSecond);
    }


    // ---------------------- TokenBucket impl ----------------------

    /**
     * 平滑突发模式：桶容量为 {@code maxBurstSeconds} 秒的许可数，已存许可可直接使用、无需额外等待。
     *
     * <p>线程安全语义继承自 {@link TokenBucket}。
     */
    static final class SmoothBursty extends TokenBucket {

        /** 桶容量对应的最大突发秒数 */
        final double maxBurstSeconds;

        /**
         * @param maxBurstSeconds 桶容量对应的最大突发秒数
         */
        SmoothBursty(double maxBurstSeconds) {
            this.maxBurstSeconds = maxBurstSeconds;
        }

        /**
         * 重算容量与已存许可：容量为 {@code maxBurstSeconds * 速率}；速率变化时按比例缩放已存许可。
         */
        @Override
        void doSetRate() {
            // 计算旧的最大许可数
            double oldMaxPermits = this.maxPermits;
            // 计算最大许可数
            maxPermits = maxBurstSeconds * permitsPerSecond;
            // 计算已存储的令牌数量
            if (oldMaxPermits == Double.POSITIVE_INFINITY) {
                // if we don't special-case this, we would get storedPermits == NaN, below
                storedPermits = maxPermits;
            } else {
                // 更改令牌桶速率时，已存储的令牌数量会按比例调整，保持令牌桶的平滑过渡
                storedPermits = (oldMaxPermits == 0.0)
                        ? 0.0 // initial state
                        : storedPermits * maxPermits / oldMaxPermits;
            }
        }

        /**
         * 平滑突发模式下已存许可无需额外等待。
         *
         * @param permitsToTake 要获取的许可数
         * @return 固定返回 0
         */
        @Override
        long storedPermitsToWaitTime(double permitsToTake) {
            return 0L;
        }

        /**
         * 令牌生成间隔等于稳定间隔。
         *
         * @return 稳定间隔，单位微秒
         */
        @Override
        double coolDownIntervalMicros() {
            return stableIntervalMicros;
        }
    }

    /**
     * 平滑预热模式：冷启动时令牌间隔为稳定间隔的 {@code coldFactor} 倍，随已存许可增加逐步缩短，
     * 在预热周期内平滑过渡到稳定速率。
     *
     * <p>线程安全语义继承自 {@link TokenBucket}。
     */
    static final class SmoothWarmingUp extends TokenBucket {

        /** 预热周期，单位微秒 */
        private final long warmupPeriodMicros;
        /** 线性斜率：已存许可从 0 增至上限时，令牌间隔从稳定间隔升至冷却间隔 */
        private double slope;
        /** 阈值许可数：不超过该值的已存许可按稳定间隔计算等待时间 */
        private double thresholdPermits;
        /** 冷却因子：冷启动间隔与稳定间隔的比值，值越大初始放行越慢 */
        private double coldFactor;

        /**
         * @param warmupPeriod 预热周期
         * @param timeUnit     预热周期单位
         * @param coldFactor   冷却因子，冷启动间隔 = 冷却因子 × 稳定间隔
         */
        SmoothWarmingUp(long warmupPeriod, TimeUnit timeUnit, double coldFactor) {
            this.warmupPeriodMicros = timeUnit.toMicros(warmupPeriod);
            this.coldFactor = coldFactor;
        }

        /**
         * 按预热周期重算阈值、容量、斜率与已存许可；速率变化时按比例缩放已存许可。
         */
        @Override
        void doSetRate() {

            // 记录旧的最大许可数
            double oldMaxPermits = maxPermits;
            // 冷启动状态下的令牌间隔时间
            double coldIntervalMicros = stableIntervalMicros * coldFactor;
            // 阈值许可数；中间点，将令牌桶分为两部分：阈值以下和阈值以上
            thresholdPermits = 0.5 * warmupPeriodMicros / stableIntervalMicros;
            // 最大许可数；确保预热周期内，令牌桶可以从冷启动状态平滑过渡到稳定状态
            maxPermits = thresholdPermits + 2.0 * warmupPeriodMicros / (stableIntervalMicros + coldIntervalMicros);
            // 计算斜率
            slope = (coldIntervalMicros - stableIntervalMicros) / (maxPermits - thresholdPermits);
            // 计算已存储的令牌数量
            if (oldMaxPermits == Double.POSITIVE_INFINITY) {
                // if we don't special-case this, we would get storedPermits == NaN, below
                storedPermits = 0.0;
            } else {
                // 更改令牌桶速率时，已存储的令牌数量会按比例调整，保持令牌桶的平滑过渡
                storedPermits = (oldMaxPermits == 0.0)
                        ? maxPermits // initial state is cold
                        : storedPermits * maxPermits / oldMaxPermits;
            }
        }

        /**
         * 按梯形面积计算获取已存许可的等待时间：阈值以下按稳定间隔线性计费，
         * 阈值以上按冷启动到稳定状态的斜坡计费。
         *
         * @param permitsToTake 要消耗的已存许可数
         * @return 需要等待的时长，单位微秒
         */
        @Override
        long storedPermitsToWaitTime(double permitsToTake) {
            // 计算阈值以上的许可数
            double availablePermitsAboveThreshold = storedPermits - thresholdPermits;
            long micros = 0;
            // 处理阈值以上的许可
            // measuring the integral on the right part of the function (the climbing line)
            if (availablePermitsAboveThreshold > 0.0) {
                // 计算需要从阈值以上部分消耗的许可数
                double permitsAboveThresholdToTake = min(availablePermitsAboveThreshold, permitsToTake);
                // 计算消耗这些许可所需的等待时间，使用梯形面积公式
                double length = permitsToTime(availablePermitsAboveThreshold) // 当前阈值以上许可对应的时间
                        + permitsToTime(availablePermitsAboveThreshold - permitsAboveThresholdToTake);  // 消耗后剩余阈值以上许可对应的时间
                // 计算需要等待的时间
                micros = (long) (permitsAboveThresholdToTake * length / 2.0);
                // 减少待消耗的许可数
                permitsToTake -= permitsAboveThresholdToTake;
            }
            // 处理阈值以下的许可
            // measuring the integral on the left part of the function (the horizontal line)
            micros += (long) (stableIntervalMicros * permitsToTake);
            return micros;
        }

        /**
         * 计算指定已存许可数对应的令牌间隔（线性插值）。
         *
         * @param permits 已存许可数
         * @return 该许可数对应的令牌间隔，单位微秒
         */
        private double permitsToTime(double permits) {
            // 稳定状态下的令牌间隔时间 + 根据许可数增加的额外时间
            return stableIntervalMicros + permits * slope;
        }

        /**
         * 冷却状态下生成一个令牌的平均间隔。
         *
         * @return {@code 预热周期 / 最大许可数}，单位微秒
         */
        @Override
        double coolDownIntervalMicros() {
            // 冷却状态下生成一个令牌所需的时间间隔
            return warmupPeriodMicros / maxPermits;
        }
    }


    // ---------------------- Stopwatch ----------------------

    /**
     * 基于 {@link System#nanoTime()} 的单调计时器，支持多次 start/stop 累加耗时。
     *
     * <p>非线程安全：仅应在单线程中使用，或由调用方自行同步。
     */
    public static final class Stopwatch {

        /**
         * 创建未启动的计时器。
         *
         * @return 未启动的计时器实例
         */
        public static Stopwatch createUnstarted() {
            return new Stopwatch();
        }

        /**
         * 创建并立即启动计时器。
         *
         * @return 已启动的计时器实例
         */
        public static Stopwatch createStarted() {
            return new Stopwatch().start();
        }

        // param
        /** 计时器是否正在运行 */
        private boolean isRunning;
        /** 已累计的耗时，单位纳秒 */
        private long elapsedNanos;
        /** 本段计时开始时的 {@link System#nanoTime()} 值 */
        private long startTick;

        Stopwatch() {
        }

        /**
         * 计时器是否正在运行。
         *
         * @return 已启动且未停止时返回 {@code true}
         */
        public boolean isRunning() {
            return isRunning;
        }

        /**
         * 启动计时；若已处于运行状态则抛出异常。
         *
         * @return 当前实例，便于链式调用
         * @throws IllegalArgumentException 计时器已在运行时抛出
         */
        public Stopwatch start() {
            AssertTool.isTrue(!isRunning, "This stopwatch is already running.");
            isRunning = true;
            startTick = System.nanoTime();
            return this;
        }

        /**
         * 停止计时，并把本段耗时累加到总耗时；若未运行则抛出异常。
         *
         * @return 当前实例，便于链式调用
         * @throws IllegalArgumentException 计时器未运行时抛出
         */
        public Stopwatch stop() {
            AssertTool.isTrue(isRunning, "This stopwatch is already stopped.");
            isRunning = false;
            elapsedNanos += System.nanoTime() - startTick;
            return this;
        }

        /**
         * 重置计时器：清空累计耗时并停止计时。
         *
         * @return 当前实例，便于链式调用
         */
        public Stopwatch reset() {
            elapsedNanos = 0;
            isRunning = false;
            return this;
        }

        /**
         * 返回累计耗时（含正在运行的当前时间段）的纳秒数。
         *
         * @return 累计耗时，单位纳秒
         */
        private long elapsedNanos() {
            return isRunning ? System.nanoTime() - startTick + elapsedNanos : elapsedNanos;
        }

        /**
         * 按指定单位返回累计耗时，小数部分按目标单位截断。
         *
         * @param desiredUnit 目标时间单位，不能为 {@code null}
         * @return 累计耗时，单位由 {@code desiredUnit} 指定
         */
        public long elapsed(TimeUnit desiredUnit) {
            return desiredUnit.convert(elapsedNanos(), NANOSECONDS);
        }

        /**
         * 以 {@link Duration} 返回累计耗时。
         *
         * @return 累计耗时
         */
        public Duration elapsed() {
            return Duration.ofNanos(elapsedNanos());
        }

        /**
         * 以最合适的单位输出紧凑格式的耗时，如 {@code 1.234 ms}。
         *
         * @return 人类可读的耗时字符串
         */
        @Override
        public String toString() {
            long nanos = elapsedNanos();

            TimeUnit unit = chooseUnit(nanos);
            double value = (double) nanos / NANOSECONDS.convert(1, unit);

            return formatCompact4Digits(value) + " " + abbreviate(unit);
        }

        private static TimeUnit chooseUnit(long nanos) {
            if (DAYS.convert(nanos, NANOSECONDS) > 0) {
                return DAYS;
            }
            if (HOURS.convert(nanos, NANOSECONDS) > 0) {
                return HOURS;
            }
            if (MINUTES.convert(nanos, NANOSECONDS) > 0) {
                return MINUTES;
            }
            if (SECONDS.convert(nanos, NANOSECONDS) > 0) {
                return SECONDS;
            }
            if (MILLISECONDS.convert(nanos, NANOSECONDS) > 0) {
                return MILLISECONDS;
            }
            if (MICROSECONDS.convert(nanos, NANOSECONDS) > 0) {
                return MICROSECONDS;
            }
            return NANOSECONDS;
        }

        private static String abbreviate(TimeUnit unit) {
            switch (unit) {
                case NANOSECONDS:
                    return "ns";
                case MICROSECONDS:
                    return "\u03bcs"; // μs
                case MILLISECONDS:
                    return "ms";
                case SECONDS:
                    return "s";
                case MINUTES:
                    return "min";
                case HOURS:
                    return "h";
                case DAYS:
                    return "d";
            }
            throw new AssertionError();
        }
    }


    // ------------------------- tool -------------------------

    /**
     * 将 {@link Duration} 转换为纳秒；溢出时饱和为 {@link Long#MIN_VALUE} 或 {@link Long#MAX_VALUE}。
     *
     * @param duration 待转换的时长，不能为 {@code null}
     * @return 纳秒数，溢出时取饱和值
     */
    public static long toNanosSaturated(Duration duration) {
        try {
            return duration.toNanos();
        } catch (ArithmeticException tooBig) {
            return duration.isNegative() ? Long.MIN_VALUE : Long.MAX_VALUE;
        }
    }

    /**
     * 以通用科学计数法格式化数值，保留 4 位有效数字。
     *
     * @param value 待格式化的数值
     * @return 格式化后的字符串
     */
    public static String formatCompact4Digits(double value) {
        return String.format(Locale.ROOT, "%.4g", value);
    }

    /**
     * 休眠指定时长且不向外抛出中断异常；若休眠期间被中断，会在结束后恢复线程的中断标记。
     *
     * @param sleepFor 休眠时长
     * @param unit     时长单位，不能为 {@code null}
     */
    public static void sleepUninterruptibly(long sleepFor, TimeUnit unit) {
        boolean interrupted = false;
        try {
            long remainingNanos = unit.toNanos(sleepFor);
            long end = System.nanoTime() + remainingNanos;
            while (true) {
                try {
                    TimeUnit.NANOSECONDS.sleep(remainingNanos);
                    return;
                } catch (InterruptedException e) {
                    interrupted = true;
                    remainingNanos = end - System.nanoTime();
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 饱和加法：溢出时向对应方向饱和到 {@link Long#MAX_VALUE} 或 {@link Long#MIN_VALUE}。
     *
     * @param a 加数
     * @param b 加数
     * @return {@code a + b}，溢出时取饱和值
     */
    public static long saturatedAdd(long a, long b) {
        long naiveSum = a + b;
        if ((a ^ b) < 0 | (a ^ naiveSum) >= 0) {
            // If a and b have different signs or a has the same sign as the result then there was no overflow, return.
            return naiveSum;
        }
        // we did over/under flow, if the sign is negative we should return MAX otherwise MIN
        return Long.MAX_VALUE + ((naiveSum >>> (Long.SIZE - 1)) ^ 1);
    }


}
