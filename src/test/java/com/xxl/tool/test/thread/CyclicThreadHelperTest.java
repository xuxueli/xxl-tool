package com.xxl.tool.test.thread;

import com.xxl.tool.core.DateTool;
import com.xxl.tool.thread.CyclicThreadHelper;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class CyclicThreadHelperTest {

    @Test
    public void test1() throws InterruptedException {

        CyclicThreadHelper threadHelper = new CyclicThreadHelper(
                "demoCyclicThread",
                true,
                1000,
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
        TimeUnit.SECONDS.sleep(3);
    }

}
