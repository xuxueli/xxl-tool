package com.xxl.tool.datastructure.bloomfilter;

import java.io.Serializable;

/**
 * 布隆过滤器多重哈希策略：把对象哈希值扩展为多个位下标，提供写入与查询操作。
 *
 * <p>策略与位数组长度、哈希函数数量共同决定位下标分布；参与合并的过滤器必须使用相同策略。
 * 内置实现无状态，可被多个过滤器/线程共享；位数组的并发安全由 {@link LockFreeBitArray} 保证。
 *
 * @author xuxueli 2025-12-13
 */
public class HashStrategies {

    // ---------------------- strategy iface ----------------------

    /**
     * 多重哈希策略。
     */
    public interface Strategy extends Serializable {

        /**
         * 计算对象对应的多个位下标并置位。
         *
         * @param object           待插入对象，不能为 {@code null}（由 Funnel 决定具体约束）
         * @param funnel           对象到字节的转换器，不能为 {@code null}
         * @param numHashFunctions 哈希函数数量，必须大于 0
         * @param bits             目标位数组，不能为 {@code null}
         * @param <T>              对象类型
         * @return 本次写入至少改变一个位时返回 {@code true}
         */
        <T extends Object> boolean put(T object,
                                       Funnels.Funnel<? super T> funnel,
                                       int numHashFunctions,
                                       LockFreeBitArray bits);

        /**
         * 计算对象对应的多个位下标并检查是否全部已置位。
         *
         * @param object           待查询对象，不能为 {@code null}（由 Funnel 决定具体约束）
         * @param funnel           对象到字节的转换器，不能为 {@code null}
         * @param numHashFunctions 哈希函数数量，必须大于 0
         * @param bits             目标位数组，不能为 {@code null}
         * @param <T>              对象类型
         * @return 所有对应位均已置位（可能存在）返回 {@code true}
         */
        <T extends Object> boolean mightContain(T object,
                                                Funnels.Funnel<? super T> funnel,
                                                int numHashFunctions,
                                                LockFreeBitArray bits);

    }


    // ---------------------- strategy impl ----------------------

    /**
     * MurmurHash3-128 MITZ 32 位变体：每个哈希位置用 32 位整数计算。
     *
     * <p>流程：取 MurmurHash3 128 位结果的两个 32 位分段，按 {@code hash1 + i * hash2} 线性组合生成
     * 多个位置，负数先取反再取模。
     */
    public static final Strategy MURMURHASH3_128_MITZ_32;

    /**
     * MurmurHash3-128 MITZ 64 位变体：每个哈希位置用 64 位整数计算。
     *
     * <p>流程：取 MurmurHash3 128 位结果的两个 64 位分段，按 {@code combinedHash += hash2} 逐次生成位置，
     * 每次先用 {@code & Long.MAX_VALUE} 清除符号位再取模。
     */
    public static final Strategy MURMURHASH3_128_MITZ_64;

    static {
        MURMURHASH3_128_MITZ_32 = new Strategy(){
            @Override
            public <T extends Object> boolean put(
                    T object,
                    Funnels.Funnel<? super T> funnel,
                    int numHashFunctions,
                    LockFreeBitArray bits) {

                // 获取对象哈希值
                long bitSize = bits.bitSize();
                long hash64 = hashObject(object, funnel).asLong();

                // 使用32位整数处理哈希
                int hash1 = (int) hash64;
                int hash2 = (int) (hash64 >>> 32);

                // 使用整数运算，通过乘法和位翻转处理负数
                boolean bitsChanged = false;
                for (int i = 1; i <= numHashFunctions; i++) {
                    // 通过线性组合生成多个哈希位置
                    int combinedHash = hash1 + (i * hash2);
                    if (combinedHash < 0) {
                        combinedHash = ~combinedHash; // 确保正数
                    }
                    bitsChanged |= bits.set(combinedHash % bitSize);
                }
                return bitsChanged;
            }

            @Override
            public <T extends Object> boolean mightContain(
                    T object,
                    Funnels.Funnel<? super T> funnel,
                    int numHashFunctions,
                    LockFreeBitArray bits) {

                // 获取对象哈希值
                long bitSize = bits.bitSize();
                long hash64 = hashObject(object, funnel).asLong();

                // 使用32位整数处理哈希
                int hash1 = (int) hash64;
                int hash2 = (int) (hash64 >>> 32);

                // 使用整数运算，通过加法和位掩码处理符号位
                for (int i = 1; i <= numHashFunctions; i++) {
                    // 通过加法生成多个哈希位置
                    int combinedHash = hash1 + (i * hash2);
                    if (combinedHash < 0) {
                        combinedHash = ~combinedHash;
                    }
                    if (!bits.get(combinedHash % bitSize)) {
                        return false;
                    }
                }
                return true;
            }
        };

        MURMURHASH3_128_MITZ_64 =  new Strategy(){
            @Override
            public <T extends Object> boolean put(
                    T object,
                    Funnels.Funnel<? super T> funnel,
                    int numHashFunctions,
                    LockFreeBitArray bits) {

                // 获取对象哈希值
                long bitSize = bits.bitSize();
                byte[] bytes = hashObject(object, funnel).getBytesInternal();

                // 将128位哈希拆分为两个64位长整数
                long hash1 = lowerEight(bytes);   // 前64位
                long hash2 = upperEight(bytes);   // 后64位

                // 使用长整数运算，通过加法和位掩码处理符号位
                boolean bitsChanged = false;
                long combinedHash = hash1;
                for (int i = 0; i < numHashFunctions; i++) {
                    // Make the combined hash positive and indexable
                    bitsChanged |= bits.set((combinedHash & Long.MAX_VALUE) % bitSize);
                    combinedHash += hash2;
                }
                return bitsChanged;
            }

            @Override
            public <T extends Object> boolean mightContain(
                    T object,
                    Funnels.Funnel<? super T> funnel,
                    int numHashFunctions,
                    LockFreeBitArray bits) {

                // 获取对象哈希值
                long bitSize = bits.bitSize();
                byte[] bytes = hashObject(object, funnel).getBytesInternal();

                // 将128位哈希拆分为两个64位长整数
                long hash1 = lowerEight(bytes);
                long hash2 = upperEight(bytes);

                // 使用长整数运算，通过加法和位掩码处理符号位
                long combinedHash = hash1;
                for (int i = 0; i < numHashFunctions; i++) {
                    if (!bits.get((combinedHash & Long.MAX_VALUE) % bitSize)) {
                        return false;
                    }
                    combinedHash += hash2;
                }
                return true;
            }

            /**
             * 提取字节数组的低8位（索引0-7），按小端序转换为long值
             */
            private long lowerEight(byte[] bytes) {
                return LongfromBytes(bytes[7], bytes[6], bytes[5], bytes[4], bytes[3], bytes[2], bytes[1], bytes[0]);
            }

            /**
             * 提取字节数组的高8位（索引8-15），按小端序转换为long值
             */
            private long upperEight(byte[] bytes) {
                return LongfromBytes(bytes[15], bytes[14], bytes[13], bytes[12], bytes[11], bytes[10], bytes[9], bytes[8]);
            }

            /**
             * 创建一个64位long值
             */
            private static long LongfromBytes(
                    byte b1, byte b2, byte b3, byte b4, byte b5, byte b6, byte b7, byte b8) {
                return (b1 & 0xFFL) << 56
                        | (b2 & 0xFFL) << 48
                        | (b3 & 0xFFL) << 40
                        | (b4 & 0xFFL) << 32
                        | (b5 & 0xFFL) << 24
                        | (b6 & 0xFFL) << 16
                        | (b7 & 0xFFL) << 8
                        | (b8 & 0xFFL);
            }
        };
    }

    /**
     * 以固定种子 0 计算对象的 MurmurHash3 128 位哈希。
     *
     * @param instance 待哈希对象
     * @param funnel   对象到字节的转换器
     * @param <T>      对象类型
     * @return 128 位哈希值
     */
    private static <T extends Object> HashCode hashObject(T instance, Funnels.Funnel<? super T> funnel) {

        // init hasher
        int seed = 0;
        AbstractHasher hasher = new Murmur3_128Hasher(seed);;

        // convert object to bytes
        funnel.funnel(instance, hasher);

        // return hash
        return hasher.hash();
    }

}
