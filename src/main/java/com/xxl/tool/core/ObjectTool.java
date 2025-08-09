package com.xxl.tool.core;

import java.util.StringJoiner;

/**
 * Object Tool
 *
 * @author xuxueli 2024-01-21 05:03:10
 */
public class ObjectTool {

    // ---------------------- judge ----------------------

    /**
     * Determines whether two possibly-null objects are equal. Returns:
     *
     * <ul>
     *   <li>{@code true} if {@code a} and {@code b} are both null.
     *   <li>{@code true} if {@code a} and {@code b} are both non-null and they are equal according to
     *       {@link Object#equals(Object)}.
     *   <li>{@code false} in all other situations.
     * </ul>
     *
     * @param a
     * @param b
     * @return
     */
    public static boolean equal(Object a, Object b) {
        return a == b || (a != null && a.equals(b));
    }

    /**
     * Determines whether the given object is an array.
     *
     * @param obj   the obj is null
     * @return
     */
    public static boolean isArray(Object obj) {
        return (obj != null && obj.getClass().isArray());
    }

    /**
     * Determine whether the given array is empty:
     *
     * @param array the array to check
     */
    public static boolean isEmpty(Object[] array) {
        return (array == null || array.length == 0);
    }

    // ---------------------- operate ----------------------

    private static final String EMPTY_STRING = "";
    private static final String NULL_STRING = "null";
    private static final String ARRAY_START = "{";
    private static final String ARRAY_END = "}";
    private static final String EMPTY_ARRAY = ARRAY_START + ARRAY_END;
    private static final String ARRAY_ELEMENT_SEPARATOR = ", ";


    /**
     * Return a String representation of the specified Object.
     *
     * @param obj   the object to build a String representation for
     * @return      a String representation of {@code obj}; Returns a {@code "null"} String if {@code obj} is {@code null}.
     */
    public static String toString(Object obj) {
        if (obj == null) {
            return NULL_STRING;
        }
        if (obj instanceof String) {
            return (String) obj;
        }
        if (obj instanceof Object[]) {
            return toString((Object[]) obj);
        }
        if (obj instanceof boolean[]) {
            return toString((boolean[]) obj);
        }
        if (obj instanceof byte[]) {
            return toString((byte[]) obj);
        }
        if (obj instanceof char[]) {
            return toString((char[]) obj);
        }
        if (obj instanceof double[]) {
            return toString((double[]) obj);
        }
        if (obj instanceof float[]) {
            return toString((float[]) obj);
        }
        if (obj instanceof int[]) {
            return toString((int[]) obj);
        }
        if (obj instanceof long[]) {
            return toString((long[]) obj);
        }
        if (obj instanceof short[]) {
            return toString((short[]) obj);
        }
        String str = obj.toString();
        return (str != null ? str : EMPTY_STRING);
    }

    /**
     * Return a String representation of the contents of the specified array.
     *
     * @param array     the array to build a String representation for
     * @return          a String representation of {@code array}
     */
    public static String toString(Object[] array) {
        if (array == null) {
            return NULL_STRING;
        }
        int length = array.length;
        if (length == 0) {
            return EMPTY_ARRAY;
        }
        StringJoiner stringJoiner = new StringJoiner(ARRAY_ELEMENT_SEPARATOR, ARRAY_START, ARRAY_END);
        for (Object o : array) {
            stringJoiner.add(String.valueOf(o));
        }
        return stringJoiner.toString();
    }

    /**
     * Return a String representation of the specified Object's identity.
     *
     * @param obj   the object to build a String representation for
     * @return      the object's identity string, like "com.xxx.xxx.XxxClass@1111"
     */
    public static String identityToString(Object obj) {
        if (obj == null) {
            return EMPTY_STRING;
        }
        return obj.getClass().getName() + "@" + getIdentityHexString(obj);
    }


    /**
     * Return a hex String form of an object's identity hash code.
     *
     * @param obj the object
     * @return the object's identity code in hex notation
     */
    public static String getIdentityHexString(Object obj) {
        return Integer.toHexString(System.identityHashCode(obj));
    }

    // ---------------------- other ----------------------


}
