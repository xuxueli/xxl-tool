package com.xxl.tool.id;

import com.xxl.tool.core.DateTool;

import java.util.Date;

/**
 * 日期型 ID 工具
 *
 * <p>ID 结构为：{@code yyyyMMddHHmmssSSS}（17 位）+ 随机数字后缀（默认 3 位），共 20 位。
 * 随机部分使用 {@link RandomIdTool#getDigitId(int)}，<b>不保证严格单调、也不保证全局唯一</b>，
 * 高并发下可能重复；需要唯一性时请自行加唯一索引或改用 {@link SnowflakeIdTool}。</p>
 *
 * <p>无状态静态方法，线程安全；不修改入参。</p>
 *
 * @author xuxueli 2025-06-29
 */
public class DateIdTool {

    /**
     * 生成日期 ID（默认 3 位随机后缀）
     *
     * @return 20 位 ID：17 位时间戳 + 3 位随机数字
     */
    public static String getDateId() {
        String dateStr = DateTool.format(new Date(), "yyyyMMddHHmmssSSS");
        String randomSuffix = RandomIdTool.getDigitId(3);
        return dateStr + randomSuffix;
    }

    /**
     * 生成日期 ID，指定随机后缀长度
     *
     * @param suffixLen 随机数字后缀长度，有效范围 1-1000
     * @return ID：17 位时间戳 + 指定长度随机数字
     * @throws IllegalArgumentException 后缀长度不在 1-1000 时抛出
     */
    public static String getDateId(int suffixLen) {
        String dateStr = DateTool.format(new Date(), "yyyyMMddHHmmssSSS");
        String randomSuffix = RandomIdTool.getDigitId(suffixLen);
        return dateStr + randomSuffix;
    }

    /*private static String getRandomSuffix() {
        int suffix = (int) (Math.random() * 1000);
        return String.format("%03d", suffix); // limit to 3 digits
    }*/

}
