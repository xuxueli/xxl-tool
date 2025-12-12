package com.xxl.tool.datastructure.bloomfilter;

import com.xxl.tool.core.AssertTool;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * streaming hasher, use buffer to process bytes
 *
 * @author xuxueli 2025-12-13
 */
public abstract class StreamingHasher extends AbstractHasher {

    /**
     * byte buffer
     * used to store incoming bytes, and will pass to hasher
     */
    private final ByteBuffer buffer;

    /**
     * buffer size
     * 缓冲区大小，表示缓冲区填满前需要处理的字节数
     */
    private final int bufferSize;

    /**
     * chunk size
     * 块大小，表示每次process()调用处理的字节数
     */
    private final int chunkSize;

    protected StreamingHasher(int chunkSize) {
        this(chunkSize, chunkSize);
    }

    protected StreamingHasher(int chunkSize, int bufferSize) {
        // valid chunkSize
        AssertTool.isTrue(bufferSize % chunkSize == 0, "bufferSize must be a multiple of chunkSize");

        // 分配缓冲区，额外的7字节用于处理数据对齐问题，使用小端序
        this.buffer = ByteBuffer.allocate(bufferSize + 7).order(ByteOrder.LITTLE_ENDIAN);

        // 每次process()调用处理的字节数，必须至少为4
        this.bufferSize = bufferSize;
        // 内部缓冲区大小，必须是chunkSize的倍数
        this.chunkSize = chunkSize;
    }

    /**
     * process byte buffer
     *
     * 1、处理缓冲区中的可用字节（最多chunkSize字节）
     * 2、由子类实现具体的哈希处理逻辑
     *
     * @param byteBuffer 包含待处理数据的ByteBuffer
     */
    protected abstract void process(ByteBuffer byteBuffer);

    @Override
    public final AbstractHasher putBytes(byte[] bytes, int off, int len) {
        return putBytesInternal(ByteBuffer.wrap(bytes, off, len).order(ByteOrder.LITTLE_ENDIAN));   // 包装字节数组为ByteBuffer，使用小端序
    }

    @Override
    public final AbstractHasher putBytes(ByteBuffer readBuffer) {
        ByteOrder order = readBuffer.order();
        try {
            readBuffer.order(ByteOrder.LITTLE_ENDIAN); // 临时设置为小端序
            return putBytesInternal(readBuffer);
        } finally {
            readBuffer.order(order);                   // 恢复原始字节序
        }
    }

    /**
     * internal putBytes
     */
    private AbstractHasher putBytesInternal(ByteBuffer readBuffer) {
        // 缓冲区空间足够，直接存入数据并检查是否需处理；
        if (readBuffer.remaining() <= buffer.remaining()) {
            buffer.put(readBuffer);
            munchIfFull();                  // 检查并处理填满的缓冲区
            return this;
        }

        // 缓冲区空间不足，先填满缓冲区并处理
        int bytesToCopy = bufferSize - buffer.position();
        for (int i = 0; i < bytesToCopy; i++) {
            buffer.put(readBuffer.get());   // 逐个字节复制
        }
        munch();                            // 处理缓冲区，此时缓冲区变空

        // 直接处理完整块
        while (readBuffer.remaining() >= chunkSize) {
            process(readBuffer);
        }

        // 处理剩余数据
        buffer.put(readBuffer);
        return this;
    }

    @Override
    public final AbstractHasher putByte(byte b) {
        buffer.put(b);
        munchIfFull();
        return this;
    }

    @Override
    public final AbstractHasher putShort(short s) {
        buffer.putShort(s);
        munchIfFull();
        return this;
    }

    @Override
    public final AbstractHasher putChar(char c) {
        buffer.putChar(c);
        munchIfFull();
        return this;
    }

    @Override
    public final AbstractHasher putInt(int i) {
        buffer.putInt(i);
        munchIfFull();
        return this;
    }

    @Override
    public final AbstractHasher putLong(long l) {
        buffer.putLong(l);
        munchIfFull();
        return this;
    }

    @Override
    public final HashCode hash() {
        munch();                // 处理缓冲区中所有完整块
        buffer.flip();          // 切换为读取模式，准备读取剩余数据
        if (buffer.remaining() > 0) {
            processRemaining(buffer);               // 处理剩余字节
            buffer.position( buffer.limit());       // 移动位置到末尾
        }
        return makeHash();      // 生成最终哈希值
    }

    /**
     * process remaining bytes
     *
     *  1、处理输入的最后字节，这些字节不足以填满一个完整的块
     *  2、默认实现使用0填充并调用process()
     *
     * @param byteBuffer 非空的ByteBuffer，包含剩余数据
     */
    protected void processRemaining(ByteBuffer byteBuffer) {
        byteBuffer.position(byteBuffer.limit());         // 移动位置到末尾
        byteBuffer.limit( chunkSize + 7);       // 扩展限制，准备用long填充
        while (byteBuffer.position() < chunkSize) {
            byteBuffer.putLong(0);                // 用0填充直到达到块大小
        }
        byteBuffer.limit(chunkSize);                    // 重置限制为块大小
        byteBuffer.flip();                              // 准备读取
        process(byteBuffer);                            // 处理填充后的数据
    }

    /**
     * make hash
     */
    protected abstract HashCode makeHash();

    /**
     * munchIfFull
     * 检查缓冲区是否接近满并处理（剩余空间小于8字节）
     */
    private void munchIfFull() {
        if (buffer.remaining() < 8) {
            munch();            // 缓冲区满或接近满，进行处理
        }
    }

    /**
     * munch
     * 处理缓冲区中的完整数据块，每次处理一个完整块
     */
    private void munch() {
        buffer.flip();          // 切换为读取模式
        while (buffer.remaining() >= chunkSize) {
            process(buffer);    // 处理每个完整块
        }
        buffer.compact();       // 压缩缓冲区，保留不完整块的数据
    }

}