package com.xxl.tool.concurrent;

import com.xxl.tool.core.DateTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * time wheel
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

    public TimeWheel(int ticksCount, long tickDuration) {
        this(ticksCount, tickDuration, 10, 200, 2000);
    }

    public TimeWheel(int ticksCount, long tickDuration, int taskExecutorCoreSize,int taskExecutorMaxSize, int taskExecutorQueueSize) {
        this(ticksCount, tickDuration, new ThreadPoolExecutor(
                taskExecutorCoreSize,
                taskExecutorMaxSize,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(taskExecutorQueueSize)));
    }

    /**
     * constructor
     *
     * @param ticksCount    ticks count
     * @param tickDuration  tick duration, by millisecond
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
    private static class TimeTask {
        private final long executeTime;
        private final Runnable task;

        public TimeTask(long executeTime, Runnable task) {
            this.executeTime = executeTime;
            this.task = task;
        }
    }

    /**
     * start time wheel
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
     * stop time wheel
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
     * submit task
     *
     * @param executeTime
     * @param task
     * @return
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
     * calculate tick
     *
     * @param executeTime
     * @return
     */
    private int calculateTick(long executeTime) {
        long delay = executeTime - System.currentTimeMillis();
        int ticks = (int) (delay / tickDuration);
        return (currentTick + ticks) % ticksCount;
    }

    /**
     * execute tasks by tick
     *
     * @param tick
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