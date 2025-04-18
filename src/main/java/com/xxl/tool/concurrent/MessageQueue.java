package com.xxl.tool.concurrent;

import com.xxl.tool.core.StringTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
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


    public MessageQueue(String name, MessageConsumer<T> consumer) {
        this(name, Integer.MAX_VALUE, consumer, 3, 1);
    }

    public MessageQueue(String name, MessageConsumer<T> consumer, int consumeBatchSize) {
        this(name, Integer.MAX_VALUE, consumer, 3, consumeBatchSize);
    }

    public MessageQueue(String name, MessageConsumer<T> consumer, int consumerCount, int consumeBatchSize) {
        this(name, Integer.MAX_VALUE, consumer, consumerCount, consumeBatchSize);
    }

    /**
     * default constructor
     *
     * @param queueLength       queue max length
     * @param consumerCount     consumer thread count
     * @param consumer          consumer method
     */
    public MessageQueue(String name, int queueLength, MessageConsumer<T> consumer, int consumerCount, int consumeBatchSize) {
        // check
        if (StringTool.isBlank(name)) {
            throw new IllegalArgumentException("name is null");
        }
        if (queueLength < 1) {
            throw new IllegalArgumentException("queueLength is invalid.");
        }
        if (consumer == null) {
            throw new IllegalArgumentException("consumer is null.");
        }
        if (consumerCount < 1) {
            throw new IllegalArgumentException("consumerCount is invalid.");
        }
        if (consumeBatchSize < 1) {
            throw new IllegalArgumentException("consumeBatchSize is invalid.");
        }


        // start
        this.name = name;
        this.messageQueue = new LinkedBlockingQueue<>(queueLength);
        this.consumerExecutor = Executors.newFixedThreadPool(consumerCount);
        this.isRunning = true;

        for (int i = 0; i < consumerCount; i++) {
            consumerExecutor.submit(() -> {
                logger.debug(">>>>>>>>>>> ProducerConsumerQueue[name = "+ name +"] consumer thead[" + Thread.currentThread().getName() + "] start.");
                while (isRunning || !messageQueue.isEmpty()) {      // running || stoped to process remaining-message
                    try {
                        T message = messageQueue.poll(3000, TimeUnit.MILLISECONDS);
                        if (message != null) {

                            // batch get message
                            List<T> messageList = new ArrayList<T>();
                            messageList.add(message);
                            if (consumeBatchSize > 1) {
                                messageQueue.drainTo(messageList, consumeBatchSize-1);
                            }

                            // batch consume
                            consumer.consume(messageList);
                        }
                    } catch (Throwable e) {
                        // error when running, print warn log
                        if (isRunning) {
                            logger.error(">>>>>>>>>>> ProducerConsumerQueue[name = "+ name +"] consumer thead[" + Thread.currentThread().getName() + "] run error:{}", e.getMessage(), e);
                        }
                        // stoped and interrupted, judge as "stop", print long
                        if (!isRunning && (e instanceof InterruptedException)) {
                            logger.error(">>>>>>>>>>> ProducerConsumerQueue[name = "+ name +"] consumer thead[" + Thread.currentThread().getName() + "] stoped and interrupted, possible stop timeout.");
                        }
                    }
                }
            });
        }

        // add shutdown-hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            stop();
        }));
        logger.info(">>>>>>>>>>> ProducerConsumerQueue[name = "+ name +"] started, with conf：queueLength = "+ queueLength +", consumerCount = "+ consumerCount +", consumeBatchSize = "+ consumeBatchSize  );
    }

    /**
     * Message Consumer
     *
     * @param <T>
     */
    public interface MessageConsumer<T> {               // extends Consumer<T>

        /**
         * batch accept message
         *
         * @param messages
         */
        void consume(List<T> messages);

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
        if (isRunning) {        // avoid repeat
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
                consumerExecutor.shutdownNow(); // force shutdown
                logger.error(">>>>>>>>>>> ProducerConsumerQueue[name = "+ name +"] stop error:{}", e.getMessage(), e);
            }
        }
    }

}