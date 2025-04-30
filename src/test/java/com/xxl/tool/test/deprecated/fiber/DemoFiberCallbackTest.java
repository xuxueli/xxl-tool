//package com.xxl.tool.test.fiber;
//
//import co.paralleluniverse.fibers.SuspendExecution;
//import com.xxl.tool.fiber.callback.AbstractFiberCallback;
//import com.xxl.tool.test.fiber.impl.DemoFiberCallback;
//import org.junit.jupiter.api.Test;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
///**
// * nomal fiber test
// *
// * @author xuxueli 2018-09-28 19:43:03
// */
//public class DemoFiberCallbackTest {
//    private static Logger logger = LoggerFactory.getLogger(DemoFiberCallbackTest.class);
//
//    @Test
//    public void test() throws SuspendExecution {
//
//        DemoService demoService = null;
//        //String result = demoService.invoke(null);
//
//        AbstractFiberCallback fiberCallback = new DemoFiberCallback();
//        demoService.invoke(null);
//        String result = (String) fiberCallback.get(3);
//
//    }
//
//    public static interface DemoService {
//        public String invoke(String param);
//    }
//
//}
