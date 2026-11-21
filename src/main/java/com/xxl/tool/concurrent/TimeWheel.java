package com.xxl.tool.concurrent;

import com.xxl.tool.core.DateTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 时间轮：按槽位（tick）批量调度定时任务，任务到期后提交到线程池执行。
 *
 * <p>槽位与精度：
 * <ul>
 *   <li>时间轮包含 {@code ticksCount} 个槽位，每格跨度 {@code tickDuration} 毫秒，可覆盖的延迟范围为
 *       {@code (0, ticksCount * tickDuration]}；已过期或超出该范围的任务不会被接受。</li>
 *   <li>后台线程按 {@code tickDuration} 推进当前槽位。启动时把基准时间对齐到整秒
 *       （{@code now - now % 1000}），而非对齐到 {@code tickDuration}；因此 {@code tickDuration}
 *       不能整除 1000 时，实际触发时刻与理论值存在偏差。</li>
 *   <li>任务只在所属槽位被扫描时判断是否到期：若扫描时尚未到期，任务会留在槽位中等待下一轮，
 *       最坏延迟约为一个完整的轮子周期（{@code ticksCount * tickDuration}）再加一个 tick。
 *       因此时间轮适合允许一定延迟的批量定时场景，不保证精确触发。</li>
 * </ul>
 *
 * <p>线程安全与生命周期：槽位使用并发容器，可在运行期间并发调用
 * {@link #submitTask(long, Runnable)}。后台轮询线程为 daemon；{@link #start()} 幂等，
 * {@link #stop()} 会关闭任务线程池并最多等待 10 秒。停止后任务线程池已关闭，再次启动无法执行任务
 * （提交会失败并记录日志）；任务线程池由调用方传入时，其关闭责任也随之转移给本实例。
 *
 * @author xuxueli 2025-04-18
 */
public class TimeWheel {
    private static Logger logger = LoggerFactory.getLogger(TimeWheel.class);

    /**
     * ticks count
     */
    private final int ticksCount;

    /**
     * ticks interval duration, by millisecond
     */
    private final long tickDuration;                        // 每刻度间隔时间(ms)

    /**
     * total duration of wheel
     */
    private final long wheelDuration;

    /**
     * task slots
     */
    private final Map<Integer, Set<TimeTask>> taskSlots;

    /**
     * current tick
     */
    private volatile int currentTick = 0;

    /**
     * last tick time
     */
    private volatile long lastTickTime;

    /**
     * running status
     */
    private final AtomicBoolean running = new AtomicBoolean(false);

    /**
     * task executor
     */
    private final ExecutorService taskExecutor;

    /**
     * 创建时间轮并使用默认任务线程池：核心 10、最大 200、队列 2000、空闲存活 60 秒。
     *
     * @param ticksCount   槽位数量，必须大于 0
     * @param tickDuration 每格跨度，单位毫秒，必须大于 0
     * @throws IllegalArgumentException 槽位数量或时间跨度小于 1 时抛出
     */
    public TimeWheel(int ticksCount, long tickDuration) {
        this(ticksCount, tickDuration, 10, 200, 2000);
    }

    /**
     * 创建时间轮并自定义任务线程池参数。
     *
     * <p>线程池使用 {@link LinkedBlockingQueue} 作为有界队列、空闲线程存活 60 秒；未显式指定拒绝策略，
     * 队列与线程都饱和时提交任务会抛出 {@link java.util.concurrent.RejectedExecutionException}，
     * 由执行线程捕获并记录日志（该次任务丢失）。
     *
     * @param ticksCount             槽位数量，必须大于 0
     * @param tickDuration           每格跨度，单位毫秒，必须大于 0
     * @param taskExecutorCoreSize   任务线程池核心线程数
     * @param taskExecutorMaxSize    任务线程池最大线程数
     * @param taskExecutorQueueSize  任务线程池队列容量
     * @throws IllegalArgumentException 槽位数量或时间跨度小于 1 时抛出
     */
    public TimeWheel(int ticksCount, long tickDuration, int taskExecutorCoreSize,int taskExecutorMaxSize, int taskExecutorQueueSize) {
        this(ticksCount, tickDuration, new ThreadPoolExecutor(
                taskExecutorCoreSize,
                taskExecutorMaxSize,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(taskExecutorQueueSize)));
    }

    /**
     * 完整构造函数：使用外部提供的任务线程池。
     *
     * <p>初始化时创建 {@code ticksCount} 个并发槽位；{@code taskExecutor} 的生命周期由本实例接管，
     * 调用 {@link #stop()} 时会被关闭。
     *
     * @param ticksCount   槽位数量，必须大于 0
     * @param tickDuration 每格跨度，单位毫秒，必须大于 0
     * @param taskExecutor 执行到期任务的线程池，不能为 {@code null}；实现未做显式校验，
     *                     为空时到期任务无法提交
     * @throws IllegalArgumentException 槽位数量或时间跨度小于 1 时抛出
     */
    public TimeWheel(int ticksCount, long tickDuration, ExecutorService taskExecutor) {
        if (ticksCount < 1 || tickDuration < 1) {
            throw new IllegalArgumentException("ticksCount and tickDuration must be greater than 0");
        }
        // init
        this.ticksCount = ticksCount;
        this.tickDuration = tickDuration;
        this.wheelDuration = ticksCount * tickDuration;
        this.taskSlots = new ConcurrentHashMap<>();
        for (int i = 0; i < ticksCount; i++) {
            taskSlots.put(i, ConcurrentHashMap.newKeySet());
        }
        this.taskExecutor = taskExecutor;
    }

    /**
     * time task
     */
    /**
     * 时间轮中的待执行任务，记录预计执行时间与任务体。
     */
    private static class TimeTask {
        /** 预计执行时间，单位毫秒 */
        private final long executeTime;
        /** 到期后执行的任务 */
        private final Runnable task;

        /**
         * @param executeTime 预计执行时间，单位毫秒
         * @param task        到期后执行的任务
         */
        public TimeTask(long executeTime, Runnable task) {
            this.executeTime = executeTime;
            this.task = task;
        }
    }

    /**
     * 启动时间轮（幂等）：首次调用创建并启动后台扫描线程，同时注册 JVM shutdown-hook。
     *
     * <p>启动时把基准时间对齐到整秒，随后后台线程按 {@code tickDuration} 推进槽位。已启动时重复调用直接返回。
     * 停止后任务线程池已关闭，再次启动无法执行到期任务。
     */
    public void start() {
        if (running.compareAndSet(false, true)) {

            // align start-time to whole second
            long now = System.currentTimeMillis();
            lastTickTime = now - (now % 1000);

            Thread wheelThread = new Thread(() -> {
                while (running.get()) {
                    long current = System.currentTimeMillis();
                    long diff = current - lastTickTime;

                    // check if enough time has passed for a tick
                    if (diff >= tickDuration) {
                        // calculate how many ticks have passed
                        int ticksToMove = (int) (diff / tickDuration);

                        // process ticks
                        for (int i = 0; i < ticksToMove; i++) {
                            int tickToProcess = (currentTick + i) % ticksCount;
                            executeTasks(tickToProcess);
                        }

                        // update current-tick and last-tick-time
                        currentTick = (currentTick + ticksToMove) % ticksCount;
                        lastTickTime = lastTickTime + (ticksToMove * tickDuration);
                    }

                    try {
                        // sleep to next tick（avoid busy-waiting）; align to whole second
                        Thread.sleep(Math.max(1, tickDuration - (System.currentTimeMillis() - lastTickTime)));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });
            wheelThread.setDaemon(true);
            wheelThread.start();

            // add shutdown-hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                stop();
            }));
            logger.info(">>>>>>>>>>> TimeWheel[hashCode = "+ this.hashCode() +"] started");
        }
    }

    /**
     * 停止时间轮：置停后台扫描线程并关闭任务线程池。
     *
     * <p>任务线程池最多等待 10 秒执行完已提交任务，超时或等待被中断时强制关闭。
     * 重复调用不会重新启动扫描线程，只会重复执行关闭流程并打印停止日志。
     */
    public void stop() {
        // mark stop
        running.set(false);
        // shutdown task-executor
        taskExecutor.shutdown();
        try {
            // wait all task finish
            if (!taskExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                // force shutdown
                taskExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            taskExecutor.shutdownNow(); // force shutdown
            logger.error(">>>>>>>>>>> TimeWheel[hashCode = "+ this.hashCode() +"] stop error:{}", e.getMessage(), e);
        }
        logger.info(">>>>>>>>>>> TimeWheel[hashCode = "+ this.hashCode() +"] stopped.");
    }

    /**
     * 提交定时任务。
     *
     * <p>仅接受延迟在 {@code (0, ticksCount * tickDuration]} 范围内的任务；提交时不校验
     * {@code task} 是否为 {@code null}，为 {@code null} 时会在执行阶段抛出空指针异常并记录日志。
     * 多个任务落在同一槽位时按到期时间逐个判断执行。
     *
     * @param executeTime 预计执行时间，单位毫秒（{@link System#currentTimeMillis()} 时间轴）
     * @param task        到期后执行的任务，不能为 {@code null}
     * @return 接受并放入槽位返回 {@code true}；执行时间已过期或超出时间轮覆盖范围返回 {@code false}
     */
    public boolean submitTask(long executeTime, Runnable task) {
        // valid
        long now = System.currentTimeMillis();
        if (executeTime <= now || executeTime > now + wheelDuration) {
            return false;
        }

        // submit task
        TimeTask timeTask = new TimeTask(executeTime, task);
        int targetTick = calculateTick(executeTime);
        taskSlots.get(targetTick).add(timeTask);
        return true;
    }

    /**
     * 计算任务目标槽位：按剩余延迟除以 tick 跨度向下取整，再加上当前槽位后取模。
     *
     * <p>向下取整会使任务可能被放入「偏早」的槽位，真正的到期判断在 {@link #executeTasks(int)} 中进行。
     *
     * @param executeTime 预计执行时间，单位毫秒
     * @return 目标槽位下标，范围 {@code [0, ticksCount)}
     */
    private int calculateTick(long executeTime) {
        long delay = executeTime - System.currentTimeMillis();
        int ticks = (int) (delay / tickDuration);
        return (currentTick + ticks) % ticksCount;
    }

    /**
     * 扫描指定槽位：把已到期任务提交到线程池并从槽位移除，未到期任务保留。
     *
     * <p>提交失败（如线程池已关闭或饱和）会记录 error 日志，该任务仍会被移除，不会重试。
     *
     * @param tick 待扫描的槽位下标
     */
    private void executeTasks(int tick) {
        Set<TimeTask> tasks = taskSlots.get(tick);
        long now = System.currentTimeMillis();

        tasks.removeIf(task -> {
            if (task.executeTime <= now) {
                try {
                    taskExecutor.submit(task.task);
                    //task.task.run();
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                return true;
            }
            return false;
        });
    }


}