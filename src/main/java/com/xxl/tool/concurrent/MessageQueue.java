package com.xxl.tool.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * producer-consumer queue
 *
 * @author xuxueli 2025-04-12
 */
public class MessageQueue<T> {
    private static Logger logger = LoggerFactory.getLogger(MessageQueue.class);

    /**
     * name
     */
    private final String name;

    /**
     * blocking queue
     */
    private final LinkedBlockingQueue<T> messageQueue;

    /**
     * consumer executor
     */
    private final ExecutorService consumerExecutor;

    /**
     * is running
     */
    private volatile boolean isRunning = true;


    public MessageQueue(String name, Consumer<T> consumer) {
        this(name, Integer.MAX_VALUE,3, consumer);
    }

    public MessageQueue(String name, int consumerCount, Consumer<T> consumer) {
        this(name, Integer.MAX_VALUE, consumerCount, consumer);
    }

    /**
     * default constructor
     *
     * @param capacity          queue max length
     * @param consumerCount     consumer thread count
     * @param consumer          consumer method
     */
    public MessageQueue(String name, int capacity, int consumerCount, Consumer<T> consumer) {
        this.name = name;
        this.messageQueue = new LinkedBlockingQueue<>(capacity);
        this.consumerExecutor = Executors.newFixedThreadPool(consumerCount);
        this.isRunning = true;

        for (int i = 0; i < consumerCount; i++) {
            consumerExecutor.submit(() -> {
                logger.info(">>>>>>>>>>> ProducerConsumerQueue[name = "+ name +"] consumer thead[+" + Thread.currentThread().getName() + "+] start.");
                while (isRunning || !messageQueue.isEmpty()) {      // running || stoped to process remaining-message
                    try {
                        T message = messageQueue.poll(3000, TimeUnit.MILLISECONDS);
                        if (message != null) {
                            consumer.accept(message);
                        }
                    } catch (Throwable e) {
                        // error when running, print warn log
                        if (isRunning) {
                            logger.error(">>>>>>>>>>> ProducerConsumerQueue[name = "+ name +"] consumer thead[+" + Thread.currentThread().getName() + "+] run error:{}", e.getMessage(), e);
                        }
                        // stoped and interrupted, judge as "stop", print long
                        if (!isRunning && (e instanceof InterruptedException)) {
                            logger.error(">>>>>>>>>>> ProducerConsumerQueue[name = "+ name +"] consumer thead[+" + Thread.currentThread().getName() + "+] stop and interrupted.");
                        }
                    }
                }
            });
        }
    }

    /**
     * produce message
     *
     * @param message
     * @return
     * @throws InterruptedException
     */
    public boolean produce(T message) {
        // check
        if (message == null || !isRunning) {
            return false;
        }

        // produce
        try {
            boolean result = messageQueue.offer(message, 20, TimeUnit.MILLISECONDS);
            if (!result) {
                logger.warn(">>>>>>>>>>> ProducerConsumerQueue[name = "+ name +"] produce fail for message: "+ String.valueOf(message));
            }
            return result;
        } catch (InterruptedException e) {
            logger.warn(">>>>>>>>>>> ProducerConsumerQueue[name = "+ name +"] produce error[" + String.valueOf(e) + "] for message: "+ String.valueOf(message));
            return false;
        }
    }

    /**
     * stop
     */
    public void stop() {
        isRunning = false;

        // stop accept new task
        consumerExecutor.shutdown();
        try {
            // wait all task finish
            if (!consumerExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                // force shutdown
                consumerExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            consumerExecutor.shutdownNow(); // 强制终止未完成的任务
            logger.error(">>>>>>>>>>> ProducerConsumerQueue[name = "+ name +"] stop error:{}", e.getMessage(), e);
        }

    }

}