//package com.xxl.tool.fiber.callback;
//
//import co.paralleluniverse.fibers.Fiber;
//import co.paralleluniverse.fibers.SuspendExecution;
//
//import java.util.concurrent.TimeUnit;
//
///**
// * abstract fiber callback
// *
// * @author xuxueli 2018-07-08 19:07:46
// */
//public abstract class AbstractFiberCallback {
//
//    private volatile Fiber fiber = Fiber.currentFiber();
//
//    public volatile int status = -1;
//    public volatile Object result;
//    public volatile Throwable exception;
//
//
//    public void onFiberSuccess(Object result) {
//        try {
//            this.result = result;
//            status = 1;
//        } finally {
//            fiber.unpark();
//        }
//    }
//
//    public void onFiberFailure(Throwable exception) {
//        try {
//            this.exception = exception;
//            status = 0;
//        } finally {
//            fiber.unpark();
//        }
//    }
//
//    public Object get(int maxWaitSeconds) throws SuspendExecution {
//        // park
//        Fiber.park(maxWaitSeconds, TimeUnit.SECONDS);
//
//        // unpark, and get fiber callback result
//        if (this.status == 1) {
//            return this.result;
//        } else if (this.status == 0) {
//            throw new RuntimeException(this.exception);
//        } else {
//            throw new RuntimeException("fiber request timeout");
//        }
//    }
//
//}
