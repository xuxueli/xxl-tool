package com.xxl.tool.datastructure.bloomfilter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static java.lang.Short.toUnsignedInt;

/**
 * Murmur3 128位哈希计算器，支持流式处理数据并生成128位哈希值
 *
 * @author xuxueli 2025-12-13
 */
public class Murmur3_128Hasher extends StreamingHasher {

    private static final int CHUNK_SIZE = 16;               // 每个处理块的大小（字节）
    private static final long C1 = 0x87c37b91114253d5L;     // 混合常数1，用于密钥混合
    private static final long C2 = 0x4cf5ad432745937fL;     // 混合常数2，用于密钥混合
    private long h1;                                        // 哈希状态变量1
    private long h2;                                        // 哈希状态变量2
    private int length;                                     // 已处理的字节总数

    /**
     * 构造函数，初始化哈希状态
     *
     * @param seed 哈希种子，用于初始化h1和h2
     */
    public Murmur3_128Hasher(int seed) {
        super(CHUNK_SIZE);  // 调用父类构造函数，设置块大小
        this.h1 = seed;     // 用种子初始化h1
        this.h2 = seed;     // 用种子初始化h2
        this.length = 0;    // 初始化已处理字节数为0
    }

    // ---------------------- process ----------------------

    /**
     * 处理一个完整的数据块（16字节）
     *
     * @param byteBuffer 包含剩余数据的ByteBuffer
     */
    @Override
    protected void process(ByteBuffer byteBuffer) {
        long k1 = byteBuffer.getLong(); // 读取块中的前8字节
        long k2 = byteBuffer.getLong(); // 读取块中的后8字节
        bmix64(k1, k2);         // 混合密钥到哈希状态
        length += CHUNK_SIZE;   // 更新已处理字节数
    }

    /**
     * 混合两个64位密钥到当前哈希状态
     *
     * @param k1 第一个64位密钥
     * @param k2 第二个64位密钥
     */
    private void bmix64(long k1, long k2) {
        h1 ^= mixK1(k1);                        // 混合k1到h1

        h1 = Long.rotateLeft(h1, 27);   // 左旋转h1 27位
        h1 += h2;                               // 将h2加到h1
        h1 = h1 * 5 + 0x52dce729;               // 线性变换h1

        h2 ^= mixK2(k2);                        // 混合k2到h2

        h2 = Long.rotateLeft(h2, 31);   // 左旋转h2 31位
        h2 += h1;                               // 将h1加到h2
        h2 = h2 * 5 + 0x38495ab5;               // 线性变换h2
    }

    // ---------------------- process remaining ----------------------

    /**
     * 处理剩余的不完整数据块（少于16字节）
     *
     * @param byteBuffer 包含剩余数据的ByteBuffer
     */
    @Override
    protected void processRemaining(ByteBuffer byteBuffer) {
        long k1 = 0;                            // 初始化第一个密钥
        long k2 = 0;                            // 初始化第二个密钥
        length += byteBuffer.remaining();       // 更新已处理字节数

        // 根据剩余字节数处理
        switch (byteBuffer.remaining()) {
            case 15:
                k2 ^= (long) toUnsignedInt(byteBuffer.get(14)) << 48; // 处理第15字节并fall through
            case 14:
                k2 ^= (long) toUnsignedInt(byteBuffer.get(13)) << 40; // 处理第14字节并fall through
            case 13:
                k2 ^= (long) toUnsignedInt(byteBuffer.get(12)) << 32; // 处理第13字节并fall through
            case 12:
                k2 ^= (long) toUnsignedInt(byteBuffer.get(11)) << 24; // 处理第12字节并fall through
            case 11:
                k2 ^= (long) toUnsignedInt(byteBuffer.get(10)) << 16; // 处理第11字节并fall through
            case 10:
                k2 ^= (long) toUnsignedInt(byteBuffer.get(9)) << 8;   // 处理第10字节并fall through
            case 9:
                k2 ^= (long) toUnsignedInt(byteBuffer.get(8));        // 处理第9字节并fall through
            case 8:
                k1 ^= byteBuffer.getLong();                           // 处理前8字节
                break;
            case 7:
                k1 ^= (long) toUnsignedInt(byteBuffer.get(6)) << 48; // 处理第7字节并fall through
            case 6:
                k1 ^= (long) toUnsignedInt(byteBuffer.get(5)) << 40; // 处理第6字节并fall through
            case 5:
                k1 ^= (long) toUnsignedInt(byteBuffer.get(4)) << 32; // 处理第5字节并fall through
            case 4:
                k1 ^= (long) toUnsignedInt(byteBuffer.get(3)) << 24; // 处理第4字节并fall through
            case 3:
                k1 ^= (long) toUnsignedInt(byteBuffer.get(2)) << 16; // 处理第3字节并fall through
            case 2:
                k1 ^= (long) toUnsignedInt(byteBuffer.get(1)) << 8;  // 处理第2字节并fall through
            case 1:
                k1 ^= (long) toUnsignedInt(byteBuffer.get(0));       // 处理第1字节
                break;
            default:
                throw new AssertionError("Should never get here."); // 不应到达此处
        }
        h1 ^= mixK1(k1);        // 混合k1到h1
        h2 ^= mixK2(k2);        // 混合k2到h2
    }

    /**
     * 混合密钥k1
     *
     * @param k1 输入密钥
     * @return 混合后的密钥
     */
    private static long mixK1(long k1) {
        k1 *= C1; // 乘以常数C1
        k1 = Long.rotateLeft(k1, 31); // 左旋转31位
        k1 *= C2; // 乘以常数C2
        return k1; // 返回混合结果
    }

    /**
     * 混合密钥k2
     *
     * @param k2 输入密钥
     * @return 混合后的密钥
     */
    private static long mixK2(long k2) {
        k2 *= C2; // 乘以常数C2
        k2 = Long.rotateLeft(k2, 33); // 左旋转33位
        k2 *= C1; // 乘以常数C1
        return k2; // 返回混合结果
    }


    // ---------------------- makeHash ----------------------

    /**
     * 生成最终的哈希码
     *
     * @return 128位哈希码
     */
    @Override
    protected HashCode makeHash() {
        h1 ^= length;       // 将长度混合到h1
        h2 ^= length;       // 将长度混合到h2

        h1 += h2;           // 混合h2到h1
        h2 += h1;           // 混合h1到h2

        h1 = fmix64(h1);    // 最终混合h1
        h2 = fmix64(h2);    // 最终混合h2

        h1 += h2;           // 再次混合h2到h1
        h2 += h1;           // 再次混合h1到h2

        return HashCode.fromBytesNoCopy(                    // 从字节数组创建哈希码，不复制数组
                ByteBuffer.wrap(new byte[CHUNK_SIZE])       // 创建16字节缓冲区
                        .order(ByteOrder.LITTLE_ENDIAN)     // 设置为小端序
                        .putLong(h1)                        // 写入h1
                        .putLong(h2)                        // 写入h2
                        .array());                          // 返回字节数组
    }

    /**
     * 最终混合函数，用于强化雪崩效应
     *
     * @param k 输入值
     * @return 混合后的值
     */
    private static long fmix64(long k) {
        k ^= k >>> 33;              // 右移并异或
        k *= 0xff51afd7ed558ccdL;   // 乘以常数
        k ^= k >>> 33;              // 右移并异或
        k *= 0xc4ceb9fe1a85ec53L;   // 乘以常数
        k ^= k >>> 33;              // 右移并异或
        return k;                   // 返回最终值
    }

}
