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
public class ProducerConsumerQueue<T> {
    private static Logger logger = LoggerFactory.getLogger(ProducerConsumerQueue.class);

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


    public ProducerConsumerQueue(String name, Consumer<T> consumer) {
        this(name, Integer.MAX_VALUE,3, consumer);
    }

    public ProducerConsumerQueue(String name, int consumerCount, Consumer<T> consumer) {
        this(name, Integer.MAX_VALUE, consumerCount, consumer);
    }

    /**
     * default constructor
     *
     * @param capacity          queue max length
     * @param consumerCount     consumer thread count
     * @param consumer          consumer method
     */
    public ProducerConsumerQueue(String name, int capacity, int consumerCount, Consumer<T> consumer) {
        this.name = name;
        this.messageQueue = new LinkedBlockingQueue<>(capacity);
        this.consumerExecutor = Executors.newFixedThreadPool(consumerCount);
        this.isRunning = true;

        for (int i = 0; i < consumerCount; i++) {
            consumerExecutor.submit(() -> {
                logger.info(">>>>>>>>>>> ProducerConsumerQueue[name = "+ name +"] start.");
                while (isRunning || !messageQueue.isEmpty()) {
                    try {
                        T item = messageQueue.poll(3000, TimeUnit.MILLISECONDS);
                        if (item != null) {
                            consumer.accept(item);
                        }
                    } catch (Throwable e) {
                        if (isRunning) {    // running, logs; stop, ignore
                            logger.error(">>>>>>>>>>> ProducerConsumerQueue[name = "+ name +"] run error:{}", e.getMessage(), e);
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
    public boolean produce(T message) throws InterruptedException {
        // check
        if (message == null) {
            return false;
        }

        // produce
        if (isRunning) {
            boolean result = messageQueue.offer(message, 20, TimeUnit.MILLISECONDS);
            if (!result) {
                logger.warn(">>>>>>>>>>> ProducerConsumerQueue[name = "+ name +"] produce fail for message: "+ String.valueOf(message));
            }
            return result;
        } else {
            return false;
        }
    }

    public void stop() {
        isRunning = false;
        consumerExecutor.shutdownNow();
    }

}