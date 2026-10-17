package com.xxl.tool.core;

/**
 * 数组工具
 *
 * <p>覆盖对象数组的空值判断、包含判断与元素下标查找；所有方法均为静态方法、无状态且线程安全，
 * 不会修改入参数组。</p>
 *
 * @author xuxueli 2025-6-22
 */
public class ArrayTool {

    /**
     * 表示未找到元素的下标值：{@code -1}。
     */
    public static final int INDEX_NOT_FOUND = -1;


    /**
     * 判断数组是否为空（null 安全）。
     *
     * <pre>
     * ArrayTool.isEmpty(null)             = true
     * ArrayTool.isEmpty(new Object[0])    = true
     * ArrayTool.isEmpty(new Object[]{1})  = false
     * </pre>
     *
     * @param array 待校验数组，可为 {@code null}
     * @return 数组为 {@code null} 或长度为 0 时返回 {@code true}
     */
    public static boolean isEmpty(final Object[] array) {
        return array==null || array.length==0;
    }

    /**
     * 判断数组是否不为空（null 安全）。
     *
     * <pre>
     * ArrayTool.isNotEmpty(null)             = false
     * ArrayTool.isNotEmpty(new Object[0])    = false
     * ArrayTool.isNotEmpty(new Object[]{1})  = true
     * </pre>
     *
     * @param array 待校验数组，可为 {@code null}
     * @return 数组非 {@code null} 且长度大于 0 时返回 {@code true}
     */
    public static boolean isNotEmpty(final Object[] array) {
        return !isEmpty(array);
    }

    /**
     * 判断数组是否包含指定元素。
     *
     * <p>等价于 {@code indexOf(array, object) != INDEX_NOT_FOUND}；数组为 {@code null}、空数组或
     * {@code object} 为 {@code null} 时返回 {@code false}。</p>
     *
     * @param array  待搜索数组，可为 {@code null}
     * @param object 待查找元素，可为 {@code null}（此时直接返回 {@code false}）
     * @return 数组中存在该元素时返回 {@code true}
     */
    public static boolean contains(final Object[] array, final Object object) {
        return indexOf(array, object) != INDEX_NOT_FOUND;
    }

    /**
     * 查找元素在数组中首次出现的下标。
     *
     * <p>使用 {@link Object#equals(Object)} 比较（{@code object.equals(array[i])}），而非 {@code ==}；
     * 数组为 {@code null}、空数组或 {@code object} 为 {@code null} 时返回 {@link #INDEX_NOT_FOUND}。
     * 因 {@code object} 为 {@code null} 时已提前返回，数组中的 {@code null} 元素不会被匹配。</p>
     *
     * <pre>
     * ArrayTool.indexOf(new String[]{"a","b"}, "b") = 1
     * ArrayTool.indexOf(new String[]{"a","b"}, "c") = -1
     * ArrayTool.indexOf(null, "a")                  = -1
     * </pre>
     *
     * @param array  待搜索数组，可为 {@code null}
     * @param object 待查找元素，可为 {@code null}
     * @return 首次出现的下标；未找到时返回 {@link #INDEX_NOT_FOUND}
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
