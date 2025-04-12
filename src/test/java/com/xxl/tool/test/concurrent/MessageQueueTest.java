package com.xxl.tool.test.concurrent;

import com.xxl.tool.concurrent.MessageQueue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.util.Assert;

import java.util.List;
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
                messages -> {
                    System.out.println("Consume: " + messages);
                },
                3,
                3
        );

        // produce
        for (int i = 0; i < 1000; i++) {
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
                messages -> {
                    for (String message : messages) {
                        consumeCount.incrementAndGet();
                        //System.out.println("Consume: " + message);
                    }
                },
                100,
                10
        );

        long startTime = System.currentTimeMillis();
        long count = 1000000;
        for (int i = 0; i < count; i++) {
            boolean ret = messageQueue.produce("test-" + i);
        }

        messageQueue.stop();
        long cost = System.currentTimeMillis() - startTime;

        Assertions.assertEquals(count, consumeCount.get());
        System.out.println("Final count = " + consumeCount.get() + ", cost = " + cost + ", tps = " + (consumeCount.get() * 1000 / cost));
    }


}
