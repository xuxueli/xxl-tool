//package com.xxl.tool.test.fiber;
//
//import co.paralleluniverse.fibers.SuspendExecution;
//import co.paralleluniverse.fibers.Suspendable;
//import co.paralleluniverse.strands.Strand;
//import co.paralleluniverse.strands.SuspendableRunnable;
//import com.xxl.tool.fiber.FiberTool;
//import org.junit.jupiter.api.Test;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.atomic.AtomicInteger;
//
///**
// * nomal fiber test
// *
// * @author xuxueli 2018-09-28 19:43:03
// */
//public class NomalFiberTest {
//    private static Logger logger = LoggerFactory.getLogger(NomalFiberTest.class);
//
//    @Test
//    public void test() {
//        final AtomicInteger result = new AtomicInteger(0);
//
//        // collection fiber task
//        List<SuspendableRunnable> fiberTaskList = new ArrayList<>();
//        for (int i = 0; i < 10000; i++) {
//            final int finalI = i;
//            fiberTaskList.add(new SuspendableRunnable() {
//                @Override
//                public void run() throws SuspendExecution, InterruptedException {
//                    if (finalI == 100) {
//                        int asd = 8 / 0;
//                    }
//
//                    int resp = mockInvoke();
//                    result.addAndGet(resp);
//
//                }
//            });
//        }
//
//        // fiber run
//        long start = System.currentTimeMillis();
//        FiberTool.submit(fiberTaskList, 50);
//        long end = System.currentTimeMillis();
//
//        logger.info("result = " + result + ", cost = " + (end - start));
//
//    }
//
//    @Suspendable
//    private int mockInvoke() throws SuspendExecution, InterruptedException {
//        Strand.sleep(3000);
//        return 1;
//    }
//
//}
