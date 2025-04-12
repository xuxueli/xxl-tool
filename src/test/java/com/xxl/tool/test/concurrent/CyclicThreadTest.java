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
                true,
                200,
                new Runnable() {
                    @Override
                    public void run() {
                        // loly biz logic
                        System.out.println("thread running at " + DateTool.formatDateTime(new Date()));
                    }
                });

        threadHelper.start();
        TimeUnit.SECONDS.sleep(3);

        threadHelper.stop();
    }

}
