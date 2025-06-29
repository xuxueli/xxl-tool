package com.xxl.tool.id;

/**
 * uuid tool
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

    public SnowflakeIdTool(long workerId) {
        if (workerId > getMaxWorkerId() || workerId < 0) {
            throw new IllegalArgumentException("workerId is illegal");
        }
        this.workerId = workerId;
    }

    /**
     * get max workerId
     * @return
     */
    private long getMaxWorkerId() {
        return ~(-1L << WORKER_BITS);
    }

    /**
     * get snowflake id
     *
     * @return
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
     * logic:
     *  1. wait until next millisecond
     *  2. check timestamp
     *  2.1 if timestamp is equal to lastTimestamp, return lastTimestamp
     *  2.2 if timestamp is greater than lastTimestamp, return timestamp
     *  2.3 if timestamp is less than lastTimestamp, return timestamp + 1
     *  3. if timestamp is greater than maxTimestamp, throw exception
     *  4. if timestamp is less than minTimestamp, throw exception
     *  5. return timestamp
     *
     * @param lastTimestamp
     * @return
     */
    private long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }

}
