package com.xxl.tool.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Cyclic Thread
 *
 * @author xuxueli 2025-04-06
 */
public class CyclicThread {
    private static Logger logger = LoggerFactory.getLogger(CyclicThread.class);

    private final String name;
    private final boolean daemon;
    private final long cyclicInterval;
    private final Runnable cyclicRunnable;
    private volatile boolean isRunning = false;

    private final Object lock = new Object();
    private final Thread workerThread;


    /**
     * constructor
     *
     * @param name
     * @param daemon            true daemon thread, false user thread; daemon thread will be killed when main thread exit.
     * @param cyclicInterval    cycle interval, for millisecond
     * @param runnable       cyclic method
     */
    public CyclicThread(String name, boolean daemon, long cyclicInterval, Runnable runnable) {
        this.name = name;
        this.daemon = daemon;
        this.cyclicInterval = cyclicInterval;
        this.cyclicRunnable = new CycleRunnable(runnable, this);
        this.isRunning = false;

        // worker
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

        public CycleRunnable(Runnable runnable, CyclicThread cyclicThread) {
            this.runnable = runnable;
            this.cyclicThread = cyclicThread;
        }

        @Override
        public void run() {
            logger.info(">>>>>>>>>>> CyclicThread[name = "+ cyclicThread.name +"] start.");
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
                        TimeUnit.MILLISECONDS.sleep(cyclicThread.cyclicInterval);
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
     * start
     */
    public void start() {
        synchronized (lock) {
            if (!isRunning) {
                // mark start
                isRunning = true;

                // do start
                workerThread.start();
            }
        }
    }

    /**
     * stop
     */
    public void stop() {
        synchronized (lock) {
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
