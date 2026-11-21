package com.xxl.tool.id;

/**
 * 雪花算法 ID 生成器
 *
 * <p>64 位组成：{@code 41 位毫秒时间戳 | 10 位 workerId | 12 位序列号}，
 * 未设置自定义起始纪元，直接使用 {@link System#currentTimeMillis()}。
 * workerId 取值范围 0-1023，同一毫秒每个实例最多生成 4096 个 ID，超出后自旋等待下一毫秒。
 * 生成结果为 {@code long}，约 19 位十进制。</p>
 *
 * <p><b>唯一性与单调性：</b>单实例内 {@link #nextId()} 为 {@code synchronized}，
 * 正常时钟下随毫秒递增且同毫秒内序列号递增；但依赖系统时钟，<b>不保证跨实例、跨重启唯一</b>，
 * 调用方需保证 workerId 全局唯一（如机器号/进程号分配）。</p>
 *
 * <p><b>时钟回拨：</b>检测到当前时间小于上一毫秒时直接抛出 {@link RuntimeException}，
 * 不做等待或备用 ID，调用方需自行处理（重试、降级或校时）。</p>
 *
 * <p><b>线程安全：</b>实例方法 {@link #nextId()} 线程安全；实例不应跨进程共享。</p>
 *
 * @author xuxueli 2025-06-29
 */
public class SnowflakeIdTool {

    /**
     * workerId
     */
    private final long workerId;

    /**
     * last timestamp
     */
    private long lastTimestamp = -1L;

    /**
     * sequence
     */
    private long sequence = 0L;

    private static final long WORKER_BITS = 10L;
    private static final long SEQUENCE_BITS = 12L;
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);

    /**
     * 创建雪花 ID 生成器
     *
     * @param workerId 工作节点 ID，有效范围 0-1023
     * @throws IllegalArgumentException {@code workerId} 超出 0-1023 时抛出
     */
    public SnowflakeIdTool(long workerId) {
        if (workerId > getMaxWorkerId() || workerId < 0) {
            throw new IllegalArgumentException("workerId is illegal");
        }
        this.workerId = workerId;
    }

    /**
     * 获取 workerId 最大值
     *
     * @return 10 位可表示的最大值 1023
     */
    private long getMaxWorkerId() {
        return ~(-1L << WORKER_BITS);
    }

    /**
     * 生成下一个雪花 ID
     *
     * <p>同一毫秒内序列号自增，达到 4095 后自旋等待下一毫秒；时钟回拨时抛出异常。</p>
     *
     * @return 雪花算法生成的 long 型 ID
     * @throws RuntimeException 检测到系统时钟回拨时抛出
     */
    public synchronized long nextId() {
        long timestamp = System.currentTimeMillis();

        if (timestamp < lastTimestamp) {
            throw new RuntimeException("时钟回拨");
        }

        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0;
        }

        lastTimestamp = timestamp;

        return (timestamp << (WORKER_BITS + SEQUENCE_BITS))
                | (workerId << SEQUENCE_BITS)
                | sequence;
    }

    /**
     * 自旋等待，直到系统时间大于上一毫秒
     *
     * @param lastTimestamp 上一次生成 ID 使用的毫秒时间戳
     * @return 大于 {@code lastTimestamp} 的当前毫秒时间戳
     */
    private long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }

}
