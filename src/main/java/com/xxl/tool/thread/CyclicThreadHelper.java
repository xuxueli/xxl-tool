package com.xxl.tool.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * thread instance
 *
 * @author xuxueli 2025-04-06
 */
public class CyclicThreadHelper {
    private static Logger logger = LoggerFactory.getLogger(CyclicThreadHelper.class);

    private String name;
    private boolean daemon;                         // true daemon thread, false user thread; daemon thread will be killed when main thread exit.
    private long waitInterval;                     // cycle interval, for millisecond
    private Runnable runnable;

    private final Object lock = new Object();
    private volatile boolean toStop = true;                // default stop
    private Thread worker = null;


    public CyclicThreadHelper(String name, boolean daemon, long waitInterval, Runnable bizRunnable) {
        this.name = name;
        this.daemon = daemon;
        this.waitInterval = waitInterval;
        this.runnable = new CycleRunnable(bizRunnable, this);
    }


    /**
     * cycle runnable
     */
    private static class CycleRunnable implements Runnable{
        private Runnable bizRunnable;
        private CyclicThreadHelper threadHelper;
        public CycleRunnable(Runnable bizRunnable, CyclicThreadHelper threadHelper) {
            this.bizRunnable = bizRunnable;
            this.threadHelper = threadHelper;
        }

        @Override
        public void run() {
            logger.info(">>>>>>>>>>> CyclicThreadHelper[name = "+ threadHelper.name +"] start.");
            while (!threadHelper.toStop) {
                // biz run
                try {
                    bizRunnable.run();
                } catch (Throwable e) {
                    if (!threadHelper.toStop) {
                        logger.error(">>>>>>>>>>> CyclicThreadHelper[name = "+ threadHelper.name +"] run error:{}", e.getMessage(), e);
                    }
                }
                // cycle wait interval
                if (!threadHelper.toStop) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(threadHelper.waitInterval);
                    } catch (Throwable e) {
                        if (!threadHelper.toStop) {
                            logger.error(">>>>>>>>>>> CyclicThreadHelper[name = "+ threadHelper.name +"] run error2:{}", e.getMessage(), e);
                        }
                    }
                }
            }
            logger.info(">>>>>>>>>>> CyclicThreadHelper[name = "+ threadHelper.name +"] stop.");
        }

    }

    /**
     * start
     */
    public void start() {
        synchronized (lock) {
            if (toStop) {
                // mark start
                toStop = false;

                // do start
                worker = new Thread(runnable);
                worker.setDaemon(daemon);
                worker.setName(name);
                worker.start();
            }
        }
    }

    /**
     * stop
     */
    public void stop() {
        synchronized (lock) {
            if (!toStop) {
                // mark stop
                toStop = true;

                // do stop
                if (worker.getState() != Thread.State.TERMINATED){
                    // interrupt and wait
                    worker.interrupt();
                    try {
                        worker.join();
                    } catch (Throwable e) {
                        logger.error(e.getMessage(), e);
                    }
                }

            }
        }
    }


}
