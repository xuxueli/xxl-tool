package com.xxl.tool.concurrent;


import com.xxl.tool.core.AssertTool;

import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.min;
import static java.util.concurrent.TimeUnit.*;

/**
 * token bucket
 * （optimized based on guava, remove unnecessary locks, improve performance in boundary cases）
 *
 * @author xuxueli 2025-09-05
 */
public abstract class TokenBucket {

    // ---------------------- create ----------------------

    /**
     * create TokenBucket with smoothBursty
     *
     * @param permitsPerSecond 每秒许可数
     * @return TokenBucket
     */
    public static TokenBucket create(double permitsPerSecond) {
        TokenBucket tokenBucket = new SmoothBursty(1.0);
        tokenBucket.setRate(permitsPerSecond);
        return tokenBucket;
    }

    /**
     * create TokenBucket with smooth-warming-up
     *
     * @param permitsPerSecond 每秒许可数
     * @param warmupPeriod     预热周期
     * @return TokenBucket
     */
    public static TokenBucket create(double permitsPerSecond, Duration warmupPeriod) {
        return create(permitsPerSecond, toNanosSaturated(warmupPeriod), TimeUnit.NANOSECONDS);
    }

    /**
     * create TokenBucket with smooth-warming-up
     *
     * @param permitsPerSecond 每秒许可数
     * @param warmupPeriod     预热周期
     * @param unit             预热周期单位
     * @return TokenBucket
     */
    public static TokenBucket create(double permitsPerSecond, long warmupPeriod, TimeUnit unit) {
        AssertTool.isTrue(warmupPeriod >= 0, "warmupPeriod must not be negative: " + warmupPeriod);
        return create(permitsPerSecond, warmupPeriod, unit, 3.0);
    }

    /**
     * create TokenBucket with smooth-warming-up
     *
     * @param permitsPerSecond 每秒许可数
     * @param warmupPeriod     预热周期
     * @param unit             预热周期单位
     * @param coldFactor       冷却因子，默认3.0，值越大冷却越慢；冷却时间 = 冷却因子 * 稳定间隔时间
     * @return TokenBucket
     */
    public static TokenBucket create(double permitsPerSecond, long warmupPeriod, TimeUnit unit, double coldFactor) {
        TokenBucket tokenBucket = new SmoothWarmingUp(warmupPeriod, unit, coldFactor);
        tokenBucket.setRate(permitsPerSecond);
        return tokenBucket;
    }

    // ---------------------- param ----------------------

    // 已存储许可证数
    protected double storedPermits;
    // 存储许可证的最大数
    protected double maxPermits;
    // 请求间隔，micros second
    protected double stableIntervalMicros;
    // 请求速率，QPS
    protected double permitsPerSecond;
    // 下个ticket授予时间
    private long nextFreeTicketMicros = 0L; // could be either in the past or future
    // 秒表
    private final Stopwatch stopwatch = Stopwatch.createStarted();
    // 线程互斥变量
    private volatile Object mutexDoNotUseDirectly;

    /**
     * 线程互斥变量
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
     * 设置令牌桶的速率
     *
     * @param permitsPerSecond 每秒许可数
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
     * 设置令牌桶的速率
     */
    abstract void doSetRate();

    /**
     * 重新同步令牌桶的状态
     *
     * @param nowMicros 当前时间
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
     * 计算必须等待的时间
     *
     * @param permitsToTake 要获取的许可数
     * @return 必须等待的时间，单位为微秒
     */
    abstract long storedPermitsToWaitTime(double permitsToTake);

    /**
     * 获取冷却时间
     *
     * @return 冷却时间间隔，单位为微秒
     */
    abstract double coolDownIntervalMicros();

    // ---------------------- acquire ----------------------

    /**
     * 阻塞获取令牌
     *
     * @return 等待时间，单位为秒
     */
    public double acquire() {
        return acquire(1);
    }

    /**
     * 阻塞获取令牌
     *
     * @param permits 请求的许可数
     * @return 等待时间，单位为秒
     */
    public double acquire(int permits) {
        long microsToWait = reserve(permits);

        if (microsToWait > 0) {
            sleepUninterruptibly(microsToWait, TimeUnit.MICROSECONDS);
        }

        return 1.0 * microsToWait / TimeUnit.SECONDS.toMicros(1L);
    }

    /**
     * 阻塞获取令牌
     *
     * @param permits 请求的许可数
     * @return 等待时间，单位为微秒
     */
    final long reserve(int permits) {
        AssertTool.isTrue(permits > 0, "Requested permits must be positive, permits:" + permits);
        synchronized (mutex()) {
            return reserveAndGetWaitLength(permits, stopwatch.elapsed(TimeUnit.MICROSECONDS));
        }
    }

    // ---------------------- tryAcquire ----------------------

    /**
     * 尝试获取令牌
     *
     * @return 是否获取到令牌
     */
    public boolean tryAcquire() {
        return tryAcquire(1, 0, TimeUnit.MICROSECONDS);
    }

    /**
     * 尝试获取令牌
     *
     * @param timeout 超时时间
     * @return 是否获取到令牌
     */
    public boolean tryAcquire(Duration timeout) {
        return tryAcquire(1, toNanosSaturated(timeout), TimeUnit.NANOSECONDS);
    }

    /**
     * 尝试获取令牌
     *
     * @param timeout 超时时间
     * @param unit    超时时间的单位
     * @return 是否获取到令牌
     */
    public boolean tryAcquire(long timeout, TimeUnit unit) {
        return tryAcquire(1, timeout, unit);
    }

    /**
     * 尝试获取令牌
     *
     * @param permits 请求的许可数
     * @return 是否获取到令牌
     */
    public boolean tryAcquire(int permits) {
        return tryAcquire(permits, 0, TimeUnit.MICROSECONDS);
    }

    /**
     * 尝试获取令牌
     *
     * @param permits 请求的许可数
     * @param timeout 超时时间
     * @return 是否获取到令牌
     */
    public boolean tryAcquire(int permits, Duration timeout) {
        return tryAcquire(permits, toNanosSaturated(timeout), TimeUnit.NANOSECONDS);
    }

    /**
     * 尝试获取令牌
     *
     * @param permits 请求的许可数
     * @param timeout 超时时间
     * @param unit    超时时间的单位
     * @return 是否获取到令牌
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
     * 指定时间内，是否可以获取令牌
     *
     * @param nowMicros     当前时间
     * @param timeoutMicros 超时时间
     * @return 是否可以获取令牌
     */
    private boolean canAcquire(long nowMicros, long timeoutMicros) {
        return nextFreeTicketMicros <= nowMicros + timeoutMicros;
    }

    /**
     * 保留ticket并返回必须等待的时间
     *
     * @param permits   请求的许可数
     * @param nowMicros 当前时间
     * @return 必须等待的时间，单位为微秒
     */
    final long reserveAndGetWaitLength(int permits, long nowMicros) {
        long momentAvailable = reserveEarliestAvailable(permits, nowMicros);
        return Math.max(momentAvailable - nowMicros, 0);
    }

    /**
     * 预定最早可用令牌，返回令牌可用时间
     *
     * @param requiredPermits 请求的许可数
     * @param nowMicros       当前时间
     * @return 令牌可用时间，单位为微秒
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

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "TokenBucket[stableRate=%3.1fqps]", permitsPerSecond);
    }


    // ---------------------- TokenBucket impl ----------------------

    static final class SmoothBursty extends TokenBucket {

        // 最大突发秒数
        final double maxBurstSeconds;

        SmoothBursty(double maxBurstSeconds) {
            this.maxBurstSeconds = maxBurstSeconds;
        }

        /**
         * 设置令牌桶的参数
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
         * 计算必须等待的时间
         *
         * @param permitsToTake 要获取的许可数
         * @return 必须等待的时间，单位为微秒
         */
        @Override
        long storedPermitsToWaitTime(double permitsToTake) {
            return 0L;
        }

        /**
         * 获取冷却时间
         *
         * @return 冷却时间，单位为微秒
         */
        @Override
        double coolDownIntervalMicros() {
            return stableIntervalMicros;
        }
    }

    static final class SmoothWarmingUp extends TokenBucket {

        // 预热周期，单位为微秒
        private final long warmupPeriodMicros;
        // 斜率，表示从稳定间隔（当permits为0时）到冷却间隔（当permits为maxPermits时）的线性函数斜率
        private double slope;
        // 阈值，表示在阈值以下的许可数不需要等待时间
        private double thresholdPermits;
        // 冷却因子，冷启动状态下的间隔时间与稳定状态下间隔时间的比例；值越大，冷启动时的速率越慢
        private double coldFactor;

        SmoothWarmingUp(long warmupPeriod, TimeUnit timeUnit, double coldFactor) {
            this.warmupPeriodMicros = timeUnit.toMicros(warmupPeriod);
            this.coldFactor = coldFactor;
        }

        /**
         * 设置令牌桶的速率
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
         * 计算必须等待的时间
         *
         * @param permitsToTake 要获取的许可数
         * @return 必须等待的时间，单位为微秒
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
         * 计算消耗阈值以上许可所需的等待时间，通过计算梯形面积来实现平滑过渡
         */
        private double permitsToTime(double permits) {
            // 稳定状态下的令牌间隔时间 + 根据许可数增加的额外时间
            return stableIntervalMicros + permits * slope;
        }

        @Override
        double coolDownIntervalMicros() {
            // 冷却状态下生成一个令牌所需的时间间隔
            return warmupPeriodMicros / maxPermits;
        }
    }


    // ---------------------- Stopwatch ----------------------

    public static final class Stopwatch {

        // create
        public static Stopwatch createUnstarted() {
            return new Stopwatch();
        }

        public static Stopwatch createStarted() {
            return new Stopwatch().start();
        }

        // param
        // 计时器是否正在运行
        private boolean isRunning;
        // 计时器已消耗的时间，单位纳秒
        private long elapsedNanos;
        // 计时器开始时间，单位纳秒
        private long startTick;

        Stopwatch() {
        }

        public boolean isRunning() {
            return isRunning;
        }

        public Stopwatch start() {
            AssertTool.isTrue(!isRunning, "This stopwatch is already running.");
            isRunning = true;
            startTick = System.nanoTime();
            return this;
        }

        public Stopwatch stop() {
            AssertTool.isTrue(isRunning, "This stopwatch is already stopped.");
            isRunning = false;
            elapsedNanos += System.nanoTime() - startTick;
            return this;
        }

        // 重置计时器
        public Stopwatch reset() {
            elapsedNanos = 0;
            isRunning = false;
            return this;
        }

        // 计算已消耗的时间，包括历史周期
        private long elapsedNanos() {
            return isRunning ? System.nanoTime() - startTick + elapsedNanos : elapsedNanos;
        }

        // 计算已消耗的时间
        public long elapsed(TimeUnit desiredUnit) {
            return desiredUnit.convert(elapsedNanos(), NANOSECONDS);
        }

        // 计算已消耗的时间
        public Duration elapsed() {
            return Duration.ofNanos(elapsedNanos());
        }

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
     * 将Duration对象转换为纳秒值，并处理可能的溢出情况
     */
    public static long toNanosSaturated(Duration duration) {
        try {
            return duration.toNanos();
        } catch (ArithmeticException tooBig) {
            return duration.isNegative() ? Long.MIN_VALUE : Long.MAX_VALUE;
        }
    }

    /**
     * 使用通用科学计数法，保留4位有效数字
     */
    public static String formatCompact4Digits(double value) {
        return String.format(Locale.ROOT, "%.4g", value);
    }

    /**
     * 休眠指定时间，不响应中断
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
     * 饱和加法
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
