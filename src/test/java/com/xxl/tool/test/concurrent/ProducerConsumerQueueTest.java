package com.xxl.tool.test.concurrent;

import com.xxl.tool.concurrent.ProducerConsumerQueue;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class ProducerConsumerQueueTest {

    public static void main(String[] args) throws InterruptedException {

        AtomicLong totalCount = new AtomicLong(0);

        ProducerConsumerQueue<Long> queue = new ProducerConsumerQueue<>(
                "demoQueue",
                10000,
                3,
                new Consumer<Long>() {
                    @Override
                    public void accept(Long data) {
                        totalCount.addAndGet(data * -1);
                        System.out.println("消费: -" + data + ", Current count : " + totalCount.get());
                    }
                }
        );

        for (int i = 0; i < 3000; i++) {
            Long addData = ThreadLocalRandom.current().nextLong(1000, 3000);
            totalCount.addAndGet(addData);
            boolean ret = queue.produce(addData);
        }
        TimeUnit.SECONDS.sleep(3);

        queue.stop();
        System.out.println("Final count = " + totalCount.get());
    }


}
