package com.xxl.tool.core;

import java.util.StringJoiner;

/**
 * Object Tool / 对象工具：提供对象判等、数组判空、toString 及对象标识输出等能力。
 *
 * <p>所有方法均为静态方法，不修改入参；类无状态，线程安全。
 *
 * @author xuxueli 2024-01-21 05:03:10
 */
public class ObjectTool {

    // ---------------------- judge ----------------------

    /**
     * 判断两个可能为 {@code null} 的对象是否相等：
     *
     * <ul>
     *   <li>两者均为 {@code null} 时返回 {@code true}；</li>
     *   <li>两者均非 {@code null} 且按 {@link Object#equals(Object)} 相等时返回 {@code true}；</li>
     *   <li>其余情况返回 {@code false}。</li>
     * </ul>
     *
     * @param a 第一个对象，可为 {@code null}
     * @param b 第二个对象，可为 {@code null}
     * @return 两个对象均为 {@code null}、或均非 {@code null} 且 {@code equals} 相等时返回 {@code true}，否则返回 {@code false}
     */
    public static boolean equal(Object a, Object b) {
        return a == b || (a != null && a.equals(b));
    }

    /**
     * 判断给定对象是否为数组（含基本类型数组）。
     *
     * @param obj 待判断的对象，可为 {@code null}
     * @return 对象为数组（含基本类型数组）时返回 {@code true}；{@code null} 或非数组返回 {@code false}
     */
    public static boolean isArray(Object obj) {
        return (obj != null && obj.getClass().isArray());
    }

    /**
     * 判断给定数组是否为空。
     *
     * @param array 待检查的数组，可为 {@code null}
     * @return 数组为 {@code null} 或长度为 {@code 0} 时返回 {@code true}，否则返回 {@code false}
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
     * 返回指定对象的字符串表示。
     *
     * <p>{@code null} 返回字符串 {@code "null"}；字符串原样返回；数组按 {@code {a, b}} 形式展开；
     * 其他对象调用 {@code toString()}，其返回 {@code null} 时转为空串。
     *
     * @param obj 待转换的对象，可为 {@code null}
     * @return 对象的字符串表示；{@code obj} 为 {@code null} 时返回字符串 {@code "null"}
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
     * 返回指定数组内容的字符串表示。
     *
     * <p>数组为 {@code null} 返回 {@code "null"}，空数组返回 {@code "{}"}，其余返回 {@code {元素1, 元素2}}；
     * 元素通过 {@link String#valueOf(Object)} 转换，{@code null} 元素输出为 {@code "null"}。
     *
     * @param array 待转换的数组，可为 {@code null}
     * @return 数组内容的字符串表示
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
     * 返回指定对象的标识字符串，格式为“类名@十六进制 identityHashCode”，如
     * {@code "com.xxx.xxx.XxxClass@1111"}。
     *
     * @param obj 待输出的对象，可为 {@code null}
     * @return 对象标识字符串；{@code obj} 为 {@code null} 时返回空串
     */
    public static String identityToString(Object obj) {
        if (obj == null) {
            return EMPTY_STRING;
        }
        return obj.getClass().getName() + "@" + getIdentityHexString(obj);
    }


    /**
     * 返回对象 identity hash code 的十六进制字符串。
     *
     * @param obj 待输出的对象，可为 {@code null}
     * @return identity hash code 的十六进制字符串；{@code obj} 为 {@code null} 时返回 {@code "0"}
     */
    public static String getIdentityHexString(Object obj) {
        return Integer.toHexString(System.identityHashCode(obj));
    }

    // ---------------------- other ----------------------


}
