package com.xxl.tool.id;

import java.util.UUID;

/**
 * uuid tool
 *
 * @author xuxueli 2025-06-29
 */
public class UUIDTool {

    /**
     * generate uuid
     *
     * @return
     */
    public static String getUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * generate simple uuid, without "-"
     *
     * @return
     */
    public static String getSimpleUUID() {
        return getUUID().replace("-", "");
    }

}
