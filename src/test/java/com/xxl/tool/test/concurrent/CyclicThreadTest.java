package com.xxl.tool.test.concurrent;

import com.xxl.tool.core.DateTool;
import com.xxl.tool.concurrent.CyclicThread;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class CyclicThreadTest {

    @Test
    public void test1() throws InterruptedException {

        CyclicThread threadHelper = new CyclicThread(
                "demoCyclicThread",
                new Runnable() {
                    @Override
                    public void run() {
                        // biz logic
                        System.out.println("thread running at " + DateTool.formatDateTime(new Date()));
                    }
                },
                200);

        threadHelper.start();
        TimeUnit.SECONDS.sleep(3);

        threadHelper.stop();
    }

    @Test
    public void test2() throws InterruptedException {
        new CyclicThread("demoCyclicThread-11111",
                new Runnable() {
                    @Override
                    public void run() {
                        System.out.println(Thread.currentThread().getName() + ": running at " + DateTool.formatDateTime(new Date()));
                    }
                },
                5*1000, true).start();
        TimeUnit.SECONDS.sleep(2);
        new CyclicThread("demoCyclicThread-22222",
                new Runnable() {
                    @Override
                    public void run() {
                        System.out.println(Thread.currentThread().getName() + ": running at " + DateTool.formatDateTime(new Date()));
                    }
                },
                5*1000, true).start();

        TimeUnit.SECONDS.sleep(20);
        //threadHelper.stop();
    }

}
