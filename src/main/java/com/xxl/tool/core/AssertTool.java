package com.xxl.tool.core;

public class AssertTool {


    /**
     * Assert a boolean expression, throwing an {@code IllegalArgumentException} if the expression evaluates to {@code false}.
     *
     * <pre class="code">
     *     Assert.isTrue(i &gt; 0, "The value must be greater than zero");
     * </pre>
     *
     * @param expression a boolean expression
     * @param message the exception message to use if the assertion fails
     * @throws IllegalArgumentException if {@code expression} is {@code false}
     */
    public static void isTrue(boolean expression, String message) {
        if (!expression) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Assert a boolean expression, throwing an {@code IllegalStateException} if the expression evaluates to {@code false}.
     *
     * @param expression a boolean expression
     * @param message the exception message to use if the assertion fails
     * @throws IllegalArgumentException if {@code expression} is {@code false}
     */
    /*public static void isTrue(boolean expression, Object message) {
        if (!expression) {
            throw new IllegalStateException(String.valueOf(message));
        }
    }*/

    /**
     * Assert that a boolean expression evaluating to {@code false} is {@code true}.
     *
     * @param expression a boolean expression
     * @param message    the exception message to use if the assertion fails
     */
    public static void isFalse(boolean expression, String message) {
        isTrue(!expression, message);
    }

    /**
     * Assert that an object is {@code null}.
     *
     * <pre>
     *     Assert.isNull(value, "The value must be null");
     * </pre>
     *
     * @param object the object to check
     * @param message the exception message to use if the assertion fails
     * @throws IllegalArgumentException if the object is not {@code null}
     */
    public static void isNull(Object object, String message) {
        if (object != null) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Assert that an object is not {@code null}.
     *
     * <pre>
     *     Assert.notNull(clazz, "The class must not be null");
     * </pre>
     *
     * @param object the object to check
     * @param message the exception message to use if the assertion fails
     * @throws IllegalArgumentException if the object is {@code null}
     */
    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Assert that a string is blank.
     *
     * @param str       the string to check
     * @param message   the exception message to use if the assertion fails
     */
    public static void isBlank(String str, String message) {
        if (StringTool.isNotBlank(str)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Assert that a string is not blank.
     *
     * @param str       the string to check
     * @param message   the exception message to use if the assertion fails
     */
    public static void notBlank(String str, String message) {
        if (StringTool.isBlank(str)) {
            throw new IllegalArgumentException(message);
        }
    }


}
