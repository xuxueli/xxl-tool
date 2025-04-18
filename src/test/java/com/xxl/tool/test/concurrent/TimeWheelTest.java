package com.xxl.tool.test.concurrent;

import com.xxl.tool.concurrent.TimeWheel;
import com.xxl.tool.core.DateTool;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TimeWheelTest {

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

}
