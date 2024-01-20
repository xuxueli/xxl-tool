package com.xxl.tool.core;

import java.util.Collection;

/**
 * Collection Tool
 *
 * @author xuxueli 2024-01-21 05:03:10
 * (some references to other libraries)
 */
public class CollectionTool {

    // ---------------------- empty  ----------------------
    public static boolean isEmpty(Collection coll) {
        return (coll == null || coll.isEmpty());
    }

    public static boolean isNotEmpty(Collection coll) {
        return !CollectionTool.isEmpty(coll);
    }

    // ---------------------- contains  ----------------------
    public static boolean contains(Collection<?> collection, Object value) {
        return isNotEmpty(collection) && collection.contains(value);
    }

}
