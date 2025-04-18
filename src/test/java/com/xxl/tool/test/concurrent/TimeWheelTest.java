package com.xxl.tool.test.concurrent;

import com.xxl.tool.concurrent.TimeWheel;
import com.xxl.tool.core.DateTool;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class TimeWheelTest {
    private static Logger logger = LoggerFactory.getLogger(TimeWheelTest.class);

    @Test
    public void test() throws InterruptedException {
        // create TimeWheel
        TimeWheel timeWheel = new TimeWheel(60, 1000);
        timeWheel.start();
        System.out.println("start at:" + DateTool.format(new Date(), "yyyy-MM-dd HH:mm:ss SSS"));

        // submitTask
        List<Integer> waitTimes = Arrays.asList(1000, 2000, 3000, 5000, 7000);
        for (Integer waitTime : waitTimes) {
            boolean result = timeWheel.submitTask(System.currentTimeMillis() + waitTime, () -> {
                System.out.println("run delay " + waitTime + " at "+ DateTool.format(new Date(), "yyyy-MM-dd HH:mm:ss SSS") +")");
            });
            Assertions.assertTrue(result);
        }

        // test long-time tast (should be false)
        boolean result = timeWheel.submitTask(System.currentTimeMillis() + 100 * 1000, () -> {
            System.out.println("This should not execute");
        });
        Assertions.assertFalse(result);

        // wait 10s
        Thread.sleep(10 * 1000);
    }

    @Test
    public void test2() throws InterruptedException {
        DelayQueue<DelayedTask> delayQueue = new DelayQueue<>();
        System.out.println("start at:" + DateTool.format(new Date(), "yyyy-MM-dd HH:mm:ss SSS"));

        // submitTask
        List<Integer> waitTimes = Arrays.asList(1000, 2000, 3000, 5000, 7000);
        for (Integer waitTime : waitTimes) {
            delayQueue.add(new DelayedTask(waitTime, () -> {
                System.out.println("run delay " + waitTime + " at "+ DateTool.format(new Date(), "yyyy-MM-dd HH:mm:ss SSS") +")");
            }));
        }

        // execute
        while (!delayQueue.isEmpty()) {
            DelayedTask task = delayQueue.take();
            task.execute();
        }
    }

    private static class DelayedTask implements Delayed {
        private long expirationTime;
        private Runnable task;

        public void execute() {
            task.run();
        }

        public DelayedTask(long delay, Runnable task) {
            this.expirationTime = System.currentTimeMillis() + delay;
            this.task = task;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return unit.convert(expirationTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(Delayed o) {
            return Long.compare(this.expirationTime, ((DelayedTask) o).expirationTime);
        }
    }

}
