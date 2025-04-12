package com.xxl.tool.test.concurrent;

import com.xxl.tool.concurrent.MessageQueue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.util.Assert;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class MessageQueueTest {

    @Test
    public void test1() throws InterruptedException {

        // make queue
        MessageQueue<String> messageQueue = new MessageQueue<>(
                "demoQueue",
                10000,
                3,
                new Consumer<String>() {
                    @Override
                    public void accept(String data) {
                        System.out.println("消费: -" + data);
                    }
                }
        );

        // produce
        for (int i = 0; i < 100; i++) {
            messageQueue.produce("Message-" + i);
        }

        // stop
        messageQueue.stop();
    }


    @Test
    public void test2(){

        AtomicLong consumeCount = new AtomicLong(0);
        MessageQueue<String> messageQueue = new MessageQueue<>(
                "demoQueue",
                10,
                new Consumer<String>() {
                    @Override
                    public void accept(String data) {
                        consumeCount.incrementAndGet();
                    }
                }
        );

        long startTime = System.currentTimeMillis();
        long count = 1000000;
        for (int i = 0; i < count; i++) {
            boolean ret = messageQueue.produce("test-" + i);
        }

        messageQueue.stop();
        long cost = System.currentTimeMillis() - startTime;

        //Assertions.assertEquals(count, consumeCount.get());
        System.out.println("Final count = " + consumeCount.get() + ", cost = " + cost + ", tps = " + (consumeCount.get() * 1000 / cost));
    }


}
