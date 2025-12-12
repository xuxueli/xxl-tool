package com.xxl.tool.datastructure.bloomfilter;

import java.io.Serializable;

/**
 * Hash Strategy
 *
 * @author xuxueli 2025-12-13
 */
public class HashStrategies {

    // ---------------------- strategy iface ----------------------

    /**
     * Hash Strategy
     */
    public interface Strategy extends Serializable {

        /**
         * put object 2 bit array
         */
        <T extends Object> boolean put(T object,
                                       Funnels.Funnel<? super T> funnel,
                                       int numHashFunctions,
                                       LockFreeBitArray bits);

        /**
         * whether the object might be contained in the Bloom filter
         */
        <T extends Object> boolean mightContain(T object,
                                                Funnels.Funnel<? super T> funnel,
                                                int numHashFunctions,
                                                LockFreeBitArray bits);

    }


    // ---------------------- strategy impl ----------------------

    /**
     * MurmurHash3-128, 32 bits per entry
     *
     *  1、使用MurmurHash3 128位哈希
     *  2、拆分为两个32位哈希值（hash1和hash2）
     *  3、通过线性组合生成多个哈希位置：hash1 + (i * hash2)
     *  4、使用位取反处理负数
     */
    public static final Strategy MURMURHASH3_128_MITZ_32;

    /**
     * MurmurHash3-128, 64 bits per entry
     *
     *  1、使用MurmurHash3 128位哈希
     *  2、拆分为两个64位哈希值
     *  3、使用位与操作确保正数：combinedHash & Long.MAX_VALUE
     *  4、通过加法生成多个哈希位置：combinedHash += hash2
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
     * hash object
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
