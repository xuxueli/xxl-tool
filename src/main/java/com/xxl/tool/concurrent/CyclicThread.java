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
    private final Runnable cyclicRunnable;
    private final long cyclicInterval;
    private final boolean alignTime;

    private volatile boolean isRunning = false;
    private final Object lock = new Object();
    private final Thread workerThread;


    public CyclicThread(String name, Runnable runnable, long cyclicInterval) {
        this(name, true, runnable, cyclicInterval, false);
    }

    public CyclicThread(String name, Runnable runnable, long cyclicInterval, boolean alignTime) {
        this(name, true, runnable, cyclicInterval, alignTime);
    }

    public CyclicThread(String name, boolean daemon, Runnable runnable, long cyclicInterval) {
        this(name, daemon, runnable, cyclicInterval, false);
    }

    /**
     * constructor
     *
     * @param name              thread name
     * @param daemon            true daemon thread, will be killed when main thread exit; false user thread.
     * @param runnable          cyclic method
     * @param cyclicInterval    cycle interval, for millisecond
     * @param alignTime         true: align time, all nodes run at the same time
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

        public CycleRunnable(Runnable runnable, CyclicThread cyclicThread) {
            this.runnable = runnable;
            this.cyclicThread = cyclicThread;
        }

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
     * start
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
     * stop
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


}
