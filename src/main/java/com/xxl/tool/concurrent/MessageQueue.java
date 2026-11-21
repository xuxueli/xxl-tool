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
 * 生产者-消费者消息队列：有界阻塞队列 + 固定数量消费线程，支持批量消费。
 *
 * <p>容量与阻塞语义：
 * <ul>
 *   <li>队列容量由 {@code queueLength} 指定，必须是有界容量；队列满时 {@link #produce(Object)}
 *       最多等待 20 毫秒，仍未入队则丢弃该条消息、打印 warn 日志并返回 {@code false}，不会无限阻塞生产者。</li>
 *   <li>消费线程以 3 秒超时轮询队列；取到一条消息后，再额外取出最多 {@code consumeBatchSize - 1} 条，
 *       一次性交给 {@link MessageConsumer#consume(List)}。批量大小为 1 时不额外取。</li>
 *   <li>{@link #stop()} 后不再接收新消息，但消费线程会继续排空队列中的剩余消息再退出；
 *       最多等待 10 秒，超时后强制关闭。</li>
 * </ul>
 *
 * <p>线程安全：{@link #produce(Object)} 可被多个生产者线程并发调用；消费回调由 {@code consumerCount}
 * 个消费线程并发执行，回调实现需自行保证线程安全。实例构造时即启动消费线程，并注册 JVM
 * shutdown-hook 调用 {@link #stop()}；正常使用时应显式调用 {@link #stop()} 释放线程。
 *
 * @param <T> 消息类型
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


    /**
     * 创建消息队列：队列容量为 {@link Integer#MAX_VALUE}（实际视为无界），3 个消费线程，批量消费大小为 1。
     *
     * @param name     队列名称，用于日志标识，不能为空白
     * @param consumer 消息消费回调，不能为 {@code null}
     * @throws IllegalArgumentException 名称为空白、或消费回调为 {@code null} 时抛出
     */
    public MessageQueue(String name, MessageConsumer<T> consumer) {
        this(name, Integer.MAX_VALUE, consumer, 3, 1);
    }

    /**
     * 创建消息队列：队列容量为 {@link Integer#MAX_VALUE}（实际视为无界），3 个消费线程。
     *
     * @param name             队列名称，用于日志标识，不能为空白
     * @param consumer         消息消费回调，不能为 {@code null}
     * @param consumeBatchSize 单次消费的最大消息条数，必须大于 0
     * @throws IllegalArgumentException 名称空白、消费回调为 {@code null} 或批量大小小于 1 时抛出
     */
    public MessageQueue(String name, MessageConsumer<T> consumer, int consumeBatchSize) {
        this(name, Integer.MAX_VALUE, consumer, 3, consumeBatchSize);
    }

    /**
     * 创建消息队列：队列容量为 {@link Integer#MAX_VALUE}（实际视为无界）。
     *
     * @param name             队列名称，用于日志标识，不能为空白
     * @param consumer         消息消费回调，不能为 {@code null}
     * @param consumerCount    消费线程数量，必须大于 0
     * @param consumeBatchSize 单次消费的最大消息条数，必须大于 0
     * @throws IllegalArgumentException 任一参数非法时抛出
     */
    public MessageQueue(String name, MessageConsumer<T> consumer, int consumerCount, int consumeBatchSize) {
        this(name, Integer.MAX_VALUE, consumer, consumerCount, consumeBatchSize);
    }

    /**
     * 完整构造函数：创建有界队列并立即启动 {@code consumerCount} 个消费线程。
     *
     * <p>构造期间会校验参数；消费线程为普通（非 daemon）线程，随后注册 shutdown-hook。
     *
     * @param name             队列名称，用于日志标识，不能为空白
     * @param queueLength      队列最大容量，必须大于 0
     * @param consumer         消息消费回调，不能为 {@code null}
     * @param consumerCount    消费线程数量，必须大于 0
     * @param consumeBatchSize 单次消费的最大消息条数，必须大于 0
     * @throws IllegalArgumentException 任一参数非法时抛出
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
     * 消息消费回调。
     *
     * <p>同一条消息只会被一个消费线程处理一次；不同批次可能被多个消费线程并发处理，
     * 实现需保证线程安全。回调抛出的异常会被捕获并记录 error 日志，不会终止消费线程。
     *
     * @param <T> 消息类型
     */
    public interface MessageConsumer<T> {               // extends Consumer<T>

        /**
         * 批量消费消息。
         *
         * <p>{@code messages} 由队列内部创建，非空且至少包含一条消息；回调不应长期持有该列表。
         *
         * @param messages 本批次待消费的消息列表，非空
         */
        void consume(List<T> messages);

    }

    /**
     * 生产（入队）一条消息。
     *
     * <p>队列满时最多等待 20 毫秒；等待期间被中断或等待超时均返回 {@code false}，消息不会入队。
     * 队列已停止（{@link #stop()} 之后）时不接受新消息。
     *
     * @param message 待入队消息；为 {@code null} 时直接返回 {@code false}
     * @return 成功入队返回 {@code true}；消息为 {@code null}、队列已停止、队列满或等待被中断返回 {@code false}
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
     * 停止消息队列（幂等）。
     *
     * <p>先将运行标记置为 {@code false}，之后 {@link #produce(Object)} 不再接受新消息；消费线程会继续
     * 排空队列中的剩余消息。随后最多等待 10 秒让消费线程结束，超时或等待被中断时调用
     * {@code shutdownNow()} 强制关闭。重复调用不会产生额外影响。
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