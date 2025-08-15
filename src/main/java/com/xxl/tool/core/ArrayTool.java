package com.xxl.tool.core;

/**
 * Array Tool
 *
 * @author xuxueli 2025-6-22
 */
public class ArrayTool {

    public static final int INDEX_NOT_FOUND = -1;


    /**
     * check array is empty
     *
     * @param array array
     * @return
     */
    public static boolean isEmpty(final Object[] array) {
        return array==null || array.length==0;
    }

    /**
     * check array is not empty
     *
     * @param array array
     * @return
     */
    public static boolean isNotEmpty(final Object[] array) {
        return !isEmpty(array);
    }

    /**
     * check array contains object
     *
     * @param array         array
     * @param object        object
     * @return
     */
    public static boolean contains(final Object[] array, final Object object) {
        return indexOf(array, object) != INDEX_NOT_FOUND;
    }

    /**
     * get index of object in array
     *
     * @param array     array
     * @param object    object
     * @return
     */
    public static int indexOf(final Object[] array, final Object object) {
        if (isEmpty(array) || object == null) {
            return INDEX_NOT_FOUND;
        }
        for (int i = 0; i < array.length; i++) {
            if (object.equals(array[i])) {
                return i;
            }
        }
        return INDEX_NOT_FOUND;
    }

}
