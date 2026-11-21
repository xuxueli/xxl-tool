package com.xxl.tool.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * 周期执行线程：按固定间隔循环执行 {@link Runnable}。
 *
 * <p>执行与时间语义：
 * <ul>
 *   <li>启动后立即执行一次，之后每轮任务执行完毕再等待间隔；间隔不扣除任务耗时，因此
 *       {@code alignTime=false} 时实际周期为「任务耗时 + cyclicInterval」。</li>
 *   <li>{@code alignTime=true} 时，每轮结束会等待到以 epoch 为基准、{@code cyclicInterval} 的整数倍时刻
 *       （即 {@code System.currentTimeMillis() % cyclicInterval == 0}）再执行下一轮；多节点使用相同间隔时
 *       可在相同时刻触发。首次执行发生在 {@link #start()} 后立即进行，不做对齐。</li>
 *   <li>运行期间任务抛出的异常会被捕获并记录 error 日志，线程不会退出，而是继续下一轮；等待期间被中断时，
 *       若仍处于运行状态同样仅记录日志。</li>
 *   <li>{@link #stop()} 会置停运行标记并 interrupt、join 工作线程，等待当前轮次结束。</li>
 * </ul>
 *
 * <p>线程安全与生命周期：{@link #start()} / {@link #stop()} 通过内部锁串行化且可重复调用；
 * {@link #isRunning()} 基于 volatile 标记。同一实例只能启动一次，停止后工作线程已终止，不能再次启动。
 * 工作线程是否为 daemon 由构造参数决定；首次调用 {@link #start()} 时注册 JVM shutdown-hook 调用 {@link #stop()}。
 *
 * @author xuxueli 2025-04-06
 */
public class CyclicThread {
    private static Logger logger = LoggerFactory.getLogger(CyclicThread.class);

    private final String name;
    private final boolean daemon;
    private final Runnable cyclicRunnable;
    private final long cyclicInterval;
    private final boolean alignTime;

    private volatile boolean isRunning = false;
    private final Object lock = new Object();
    private final Thread workerThread;


    /**
     * 创建 daemon 周期线程，不做时间对齐。
     *
     * @param name           线程名称
     * @param runnable       每轮执行的业务逻辑
     * @param cyclicInterval 循环间隔，单位毫秒，应大于 0
     */
    public CyclicThread(String name, Runnable runnable, long cyclicInterval) {
        this(name, true, runnable, cyclicInterval, false);
    }

    /**
     * 创建 daemon 周期线程。
     *
     * @param name           线程名称
     * @param runnable       每轮执行的业务逻辑
     * @param cyclicInterval 循环间隔，单位毫秒，应大于 0
     * @param alignTime      {@code true} 时按时间边界对齐执行，{@code false} 时按固定间隔顺延
     */
    public CyclicThread(String name, Runnable runnable, long cyclicInterval, boolean alignTime) {
        this(name, true, runnable, cyclicInterval, alignTime);
    }

    /**
     * 创建周期线程，不做时间对齐。
     *
     * @param name           线程名称
     * @param daemon         {@code true} 为 daemon 线程，随 JVM 退出；{@code false} 为用户线程
     * @param runnable       每轮执行的业务逻辑
     * @param cyclicInterval 循环间隔，单位毫秒，应大于 0
     */
    public CyclicThread(String name, boolean daemon, Runnable runnable, long cyclicInterval) {
        this(name, daemon, runnable, cyclicInterval, false);
    }

    /**
     * 完整构造函数：创建并配置工作线程，但不启动，需调用 {@link #start()}。
     *
     * @param name           线程名称
     * @param daemon         {@code true} 为 daemon 线程，随 JVM 退出；{@code false} 为用户线程
     * @param runnable       每轮执行的业务逻辑
     * @param cyclicInterval 循环间隔，单位毫秒，应大于 0；实现未做参数校验，非正数会导致时间对齐或休眠异常
     * @param alignTime      {@code true} 时按 epoch 时间边界对齐执行，{@code false} 时按固定间隔顺延
     */
    public CyclicThread(String name, boolean daemon, Runnable runnable, long cyclicInterval, boolean alignTime) {
        this.name = name;
        this.daemon = daemon;
        this.cyclicRunnable = new CycleRunnable(runnable, this);
        this.cyclicInterval = cyclicInterval;
        this.alignTime = alignTime;

        // worker
        this.isRunning = false;
        this.workerThread = new Thread(cyclicRunnable);
        this.workerThread.setDaemon(daemon);
        this.workerThread.setName(name);
    }


    /**
     * cycle runnable
     */
    private static class CycleRunnable implements Runnable{
        private final Runnable runnable;
        private final CyclicThread cyclicThread;

        /**
         * @param runnable     业务逻辑
         * @param cyclicThread 所属周期线程，用于读取运行状态与配置
         */
        public CycleRunnable(Runnable runnable, CyclicThread cyclicThread) {
            this.runnable = runnable;
            this.cyclicThread = cyclicThread;
        }

        /**
         * 循环执行 {@code runnable}，直到所属线程被停止。
         */
        @Override
        public void run() {
            logger.info(">>>>>>>>>>> CyclicThread[name = "+ cyclicThread.name +"] start.");

            /*// align time
            if (cyclicThread.alignTime) {
                long startTime = System.currentTimeMillis();
                try {
                    TimeUnit.MILLISECONDS.sleep(cyclicThread.cyclicInterval - (startTime % cyclicThread.cyclicInterval));
                } catch (Throwable e) {
                    if (cyclicThread.isRunning) {
                        logger.error(">>>>>>>>>>> CyclicThread[name = "+ cyclicThread.name +"] run error3:{}",e.getMessage(), e);
                    }
                }
            }*/

            // loop
            while (cyclicThread.isRunning) {
                // biz run
                try {
                    runnable.run();
                } catch (Throwable e) {
                    if (cyclicThread.isRunning) {
                        logger.error(">>>>>>>>>>> CyclicThread[name = "+ cyclicThread.name +"] run error:{}", e.getMessage(), e);
                    }
                }
                // cycle wait interval
                if (cyclicThread.isRunning) {
                    try {
                        // align time
                        if (cyclicThread.alignTime) {
                            long startTime = System.currentTimeMillis();
                            TimeUnit.MILLISECONDS.sleep(cyclicThread.cyclicInterval - (startTime % cyclicThread.cyclicInterval));
                        } else {
                            TimeUnit.MILLISECONDS.sleep(cyclicThread.cyclicInterval);
                        }
                    } catch (Throwable e) {
                        if (cyclicThread.isRunning) {
                            logger.error(">>>>>>>>>>> CyclicThread[name = "+ cyclicThread.name +"] run error2:{}", e.getMessage(), e);
                        }
                    }
                }
            }
            logger.info(">>>>>>>>>>> CyclicThread[name = "+ cyclicThread.name +"] stop.");
        }

    }

    /**
     * 启动周期线程（幂等）：已启动时直接返回，不重复创建线程。
     *
     * <p>首次启动会注册 JVM shutdown-hook 用于停止线程；启动后立即执行第一轮业务逻辑。
     */
    public void start() {
        synchronized (lock) {
            if (!isRunning) {
                // mark start
                isRunning = true;

                // do start
                workerThread.start();

                // add shutdown-hook
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    stop();
                }));
            }
        }
    }

    /**
     * 停止周期线程（幂等）：置停运行标记，interrupt 并等待工作线程结束。
     *
     * <p>未启动或已停止时直接返回。若当前线程被中断，仅记录日志并继续，不向外抛异常。
     * 停止时 join 工作线程没有超时；若业务逻辑不响应中断，{@link #stop()} 可能长时间阻塞。
     * 停止后该实例不能再次启动。
     */
    public void stop() {
        synchronized (lock) {
            if (isRunning) {    // avoid repeat
                // mark stop
                isRunning = false;

                // do stop
                if (workerThread.getState() != Thread.State.TERMINATED){
                    // interrupt and wait
                    workerThread.interrupt();
                    try {
                        workerThread.join();
                    } catch (Throwable e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        }
    }

    /**
     * 是否处于运行状态。
     *
     * @return 已调用 {@link #start()} 且尚未调用 {@link #stop()} 时返回 {@code true}
     */
    public boolean isRunning() {
        return isRunning;
    }

}
