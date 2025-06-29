package com.xxl.tool.id;

import com.xxl.tool.core.DateTool;

import java.util.Date;

/**
 * date id tool
 *
 * @author xuxueli 2025-06-29
 */
public class DateIdTool {

    /**
     * get date id
     *
     * @return
     */
    public static String getDateId() {
        String dateStr = DateTool.format(new Date(), "yyyyMMddHHmmssSSS");
        String randomSuffix = RandomIdTool.getDigitId(3);
        return dateStr + randomSuffix;
    }

    /**
     * get date id, with suffix length
     *
     * @param suffixLen
     * @return
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
