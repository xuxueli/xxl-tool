package com.xxl.tool.core;

/**
 * 断言工具：用于对参数或状态做前置校验，校验失败时抛出 {@link IllegalArgumentException}。
 *
 * <p>所有方法均为静态方法，不修改入参；类无状态，线程安全。
 *
 * <p>空值语义：{@code isBlank} / {@code notBlank} 将 {@code null} 视为空白字符串；
 * 其余方法按各自 Javadoc 说明处理 {@code null}。
 */
public class AssertTool {


    /**
     * 断言布尔表达式为 {@code true}，否则抛出 {@link IllegalArgumentException}。
     *
     * <pre class="code">
     *     AssertTool.isTrue(i &gt; 0, "The value must be greater than zero");
     * </pre>
     *
     * @param expression 待校验的布尔表达式
     * @param message    断言失败时使用的异常信息，可为 {@code null}
     * @throws IllegalArgumentException 当 {@code expression} 为 {@code false} 时抛出
     */
    public static void isTrue(boolean expression, String message) {
        if (!expression) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 断言布尔表达式为 {@code false}，否则抛出 {@link IllegalArgumentException}。
     *
     * <pre>
     *     AssertTool.isFalse(flag, "The flag must be false");
     * </pre>
     *
     * @param expression 待校验的布尔表达式
     * @param message    断言失败时使用的异常信息，可为 {@code null}
     * @throws IllegalArgumentException 当 {@code expression} 为 {@code true} 时抛出
     */
    public static void isFalse(boolean expression, String message) {
        isTrue(!expression, message);
    }

    /**
     * 断言对象为 {@code null}，否则抛出 {@link IllegalArgumentException}。
     *
     * <pre>
     *     AssertTool.isNull(value, "The value must be null");
     * </pre>
     *
     * @param object  待校验的对象，可为 {@code null}
     * @param message 断言失败时使用的异常信息，可为 {@code null}
     * @throws IllegalArgumentException 当对象不为 {@code null} 时抛出
     */
    public static void isNull(Object object, String message) {
        if (object != null) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 断言对象不为 {@code null}，否则抛出 {@link IllegalArgumentException}。
     *
     * <pre>
     *     AssertTool.notNull(clazz, "The class must not be null");
     * </pre>
     *
     * @param object  待校验的对象，可为 {@code null}
     * @param message 断言失败时使用的异常信息，可为 {@code null}
     * @throws IllegalArgumentException 当对象为 {@code null} 时抛出
     */
    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 断言字符串为空白（{@code null}、空串或仅包含空白字符），否则抛出 {@link IllegalArgumentException}。
     *
     * @param str     待校验的字符串，可为 {@code null}
     * @param message 断言失败时使用的异常信息，可为 {@code null}
     * @throws IllegalArgumentException 当字符串非空白（{@link StringTool#isNotBlank(String)} 为 {@code true}）时抛出
     */
    public static void isBlank(String str, String message) {
        if (StringTool.isNotBlank(str)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 断言字符串非空白（{@code null}、空串或仅包含空白字符均视为空白），否则抛出 {@link IllegalArgumentException}。
     *
     * @param str     待校验的字符串，可为 {@code null}
     * @param message 断言失败时使用的异常信息，可为 {@code null}
     * @throws IllegalArgumentException 当字符串为空白（{@link StringTool#isBlank(String)} 为 {@code true}）时抛出
     */
    public static void notBlank(String str, String message) {
        if (StringTool.isBlank(str)) {
            throw new IllegalArgumentException(message);
        }
    }


}
