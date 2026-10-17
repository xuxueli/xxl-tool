package com.xxl.tool.core;

import java.lang.reflect.*;

/**
 * Reflection Tool / 反射工具：封装方法、字段的查找、访问与调用，以及 JDK 动态代理创建。
 *
 * <p>覆盖范围：
 * <ul>
 *   <li>方法查找：区分「当前类及父类的 public 方法」与「当前类声明的全部方法」（{@link #getMethod(Class, String, Class[])}）；</li>
 *   <li>字段查找：区分 {@link Class#getField(String)} 与 {@link Class#getDeclaredField(String)}（{@link #getField(Class, String)}）；</li>
 *   <li>访问控制与读写：{@link #makeAccessible(Method)}、{@link #makeAccessible(Field)}、{@link #getFieldValue}、{@link #setFieldValue}；</li>
 *   <li>按继承层次遍历字段（{@link #doWithFields}）与动态代理（{@link #newProxy}）。</li>
 * </ul>
 *
 * <p>所有方法均为静态方法，无共享可变状态。需要注意的是：{@code makeAccessible}、{@code getFieldValue}、
 * {@code setFieldValue} 会修改 {@link Method}/{@link Field} 实例的 accessible 标记，该标记对持有同一反射对象的所有调用方可见，
 * 并发使用时需由调用方保证一致性。
 *
 * @author xuxueli 2024-01-21 05:03:10
 */
public class ReflectionTool {


    // ---------------------- find method ----------------------

    /**
     * 查找方法：使用 {@link Class#getMethod(String, Class[])}，即当前类及父类中声明的 public 方法。
     *
     * <p>等价于 {@code getMethod(clazz, methodName, paramTypes, true, true)}，未找到时返回 {@code null}。
     *
     * @param clazz         待查找的类，不可为 {@code null}
     * @param methodName    方法名，不可为 {@code null}
     * @param paramTypes    参数类型，可为 {@code null}；{@code null} 等价于空数组，即只匹配无参方法
     * @return              找到的方法；未找到时返回 {@code null}
     * @throws IllegalArgumentException {@code clazz} 或 {@code methodName} 为 {@code null} 时抛出
     */
    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        return getMethod(clazz, methodName, paramTypes, true, true);
    }

    /**
     * 查找当前类声明的方法：使用 {@link Class#getDeclaredMethod(String, Class[])}，包含 private/protected，不包含继承方法。
     *
     * <p>等价于 {@code getMethod(clazz, methodName, paramTypes, false, true)}，未找到时返回 {@code null}。
     *
     * @param clazz         待查找的类，不可为 {@code null}
     * @param methodName    方法名，不可为 {@code null}
     * @param paramTypes    参数类型，可为 {@code null}；{@code null} 等价于空数组，即只匹配无参方法
     * @return              找到的方法；未找到时返回 {@code null}
     * @throws IllegalArgumentException {@code clazz} 或 {@code methodName} 为 {@code null} 时抛出
     */
    public static Method getDeclaredMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        return getMethod(clazz, methodName, paramTypes, false, true);
    }

    /**
     * 查找方法，可选择查找范围与未找到时的行为。
     *
     * <p>{@code getMethodOrDeclared} 为 {@code true} 时使用 {@link Class#getMethod(String, Class[])}，
     * 返回 public 方法（含父类）；为 {@code false} 时使用 {@link Class#getDeclaredMethod(String, Class[])},
     * 仅返回当前类声明的方法（任意访问级别）。方法名与参数类型必须完全匹配，不做参数自动装箱/拓宽匹配。
     *
     * @param clazz               待查找的类，不可为 {@code null}
     * @param methodName          方法名，不可为 {@code null}
     * @param paramTypes          参数类型，可为 {@code null}；{@code null} 等价于空数组，即只匹配无参方法
     * @param getMethodOrDeclared {@code true} 查询 public 方法（含继承），{@code false} 查询当前类声明的方法
     * @param ignoreIfNotFound    {@code true} 未找到返回 {@code null}；{@code false} 抛出 {@link IllegalStateException}
     * @return 找到的方法；未找到且 {@code ignoreIfNotFound} 为 {@code true} 时返回 {@code null}
     * @throws IllegalArgumentException {@code clazz} 或 {@code methodName} 为 {@code null} 时抛出
     * @throws IllegalStateException    未找到方法且 {@code ignoreIfNotFound} 为 {@code false} 时抛出
     */
    public static Method getMethod(Class<?> clazz,
                                    String methodName,
                                    Class<?>[] paramTypes,
                                    boolean getMethodOrDeclared,
                                    boolean ignoreIfNotFound) {
        AssertTool.notNull(clazz, "Class must not be null");
        AssertTool.notNull(methodName, "Method name must not be null");

        try {
            /**
             * getMethods：
             *      - include super class
             *      - only public
             * getDeclaredMethods：
             *      - current class
             *      - all methods, public/protected/private
             */
            return getMethodOrDeclared
                    ? clazz.getMethod(methodName, paramTypes)
                    : clazz.getDeclaredMethod(methodName, paramTypes);
        } catch (NoSuchMethodException ex) {
            // not found
            if (ignoreIfNotFound) {
                return null;
            }
            throw new IllegalStateException("Expected method not found: " + ex);
        }
        /*Method[] methods = getMethodOrDeclared
                ? clazz.getMethods()
                : clazz.getDeclaredMethods();
        // filter special method
        Set<Method> candidates = Arrays.stream(methods)
                .filter(method -> methodName.equals(method.getName()))
                .collect(Collectors.toSet());
        if (candidates.size() == 1) {
            return candidates.iterator().next();
        }
        // not found or multiple
        if (ignoreIfNotFound) {
            return null;
        }
        if (candidates.isEmpty()) {
            throw new IllegalStateException("Expected method not found: " + clazz.getName() + '.' + methodName);
        } else {
            throw new IllegalStateException("No unique method found: " + clazz.getName() + '.' + methodName);
        }*/
    }

    /**
     * 获取方法数组。
     *
     * <p>{@code getMethodOrDeclared} 为 {@code true} 时返回当前类及父类的 public 方法；
     * 为 {@code false} 时仅返回当前类声明的方法（含 private/protected），不包含继承方法。
     *
     * @param clazz               目标类，不可为 {@code null}
     * @param getMethodOrDeclared {@code true} 返回 public 方法（含继承），{@code false} 返回当前类声明的方法
     * @return 方法数组，无方法时返回空数组
     * @throws NullPointerException {@code clazz} 为 {@code null} 时抛出（本方法未做参数断言）
     */
    public static Method[] getMethods(Class<?> clazz, boolean getMethodOrDeclared) {
        return getMethodOrDeclared
                ? clazz.getMethods()
                : clazz.getDeclaredMethods();
    }


    // ---------------------- method operate ----------------------

    /**
     * 打开方法访问权限：仅当方法或其声明类不是 public 时，调用
     * {@link java.lang.reflect.AccessibleObject#trySetAccessible()} 尝试开启访问。
     *
     * <p>会修改 {@link Method} 实例的 accessible 标记（对同一实例全局可见）；public 方法且声明类为 public 时不做任何操作。
     * 目标类位于未开放反射访问的模块、导致无法开启访问时，抛出
     * {@link java.lang.reflect.InaccessibleObjectException}。
     *
     * @param method 待处理的方法，不可为 {@code null}
     * @throws java.lang.reflect.InaccessibleObjectException 无法开启访问权限时抛出
     */
    public static void makeAccessible(Method method) {
        if ((!Modifier.isPublic(method.getModifiers())
                || !Modifier.isPublic(method.getDeclaringClass().getModifiers()))
            && !method.trySetAccessible()) {
            throw new InaccessibleObjectException("Unable to make method accessible: " + method);
        }
    }

    /**
     * 调用方法并返回结果。
     *
     * <p>{@code args} 为 {@code null} 时按无参方法调用，否则按传入参数调用。调用前不会自动打开访问权限，
     * 非 public 方法需先调用 {@link #makeAccessible(Method)}，否则会因访问受限而失败。
     *
     * <p>访问失败、参数不匹配以及目标方法内部抛出的异常，统一包装为 {@link RuntimeException}
     * （{@link java.lang.reflect.InvocationTargetException} 不会被解包）。
     *
     * @param method 待调用的方法，不可为 {@code null}
     * @param target 目标对象；调用 static 方法时可为 {@code null}
     * @param args   方法参数，可为 {@code null}（等价于无参调用）
     * @return 方法返回值；目标方法返回 {@code void} 时为 {@code null}
     * @throws RuntimeException 调用失败或目标方法抛出异常时抛出
     */
    public static Object invokeMethod(Method method, Object target, Object... args) {
        try {
            if (args != null) {
                return method.invoke(target, args);
            } else {
                return method.invoke(target);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to invoke method [" + method + "]", ex);
        }
    }


    // ---------------------- find field ----------------------

    /**
     * 查找字段：使用 {@link Class#getField(String)}，即当前类及父类中声明的 public 字段。
     *
     * <p>等价于 {@code getField(clazz, fieldName, true, true)}，未找到时返回 {@code null}。
     *
     * @param clazz     待查找的类，不可为 {@code null}
     * @param fieldName 字段名，不可为 {@code null}
     * @return 找到的字段；未找到时返回 {@code null}
     * @throws IllegalArgumentException {@code clazz} 或 {@code fieldName} 为 {@code null} 时抛出
     */
    public static Field getField(Class<?> clazz, String fieldName) {
        return getField(clazz, fieldName, true, true);
    }

    /**
     * 查找当前类声明的字段：使用 {@link Class#getDeclaredField(String)}，包含 private/protected，不包含继承字段。
     *
     * <p>等价于 {@code getField(clazz, fieldName, false, true)}，未找到时返回 {@code null}。
     *
     * @param clazz     待查找的类，不可为 {@code null}
     * @param fieldName 字段名，不可为 {@code null}
     * @return 找到的字段；未找到时返回 {@code null}
     * @throws IllegalArgumentException {@code clazz} 或 {@code fieldName} 为 {@code null} 时抛出
     */
    public static Field getDeclaredField(Class<?> clazz, String fieldName) {
        return getField(clazz, fieldName, false, true);
    }

    /**
     * 查找字段，可选择查找范围与未找到时的行为。
     *
     * <p>{@code getFieldOrDeclared} 为 {@code true} 时使用 {@link Class#getField(String)}，
     * 返回 public 字段（含父类）；为 {@code false} 时使用 {@link Class#getDeclaredField(String)}，
     * 仅返回当前类声明的字段（任意访问级别）。
     *
     * @param clazz              待查找的类，不可为 {@code null}
     * @param fieldName          字段名，不可为 {@code null}
     * @param getFieldOrDeclared {@code true} 查询 public 字段（含继承），{@code false} 查询当前类声明的字段
     * @param ignoreIfNotFound   {@code true} 未找到返回 {@code null}；{@code false} 抛出 {@link IllegalStateException}
     * @return 找到的字段；未找到且 {@code ignoreIfNotFound} 为 {@code true} 时返回 {@code null}
     * @throws IllegalArgumentException {@code clazz} 或 {@code fieldName} 为 {@code null} 时抛出
     * @throws IllegalStateException    未找到字段且 {@code ignoreIfNotFound} 为 {@code false} 时抛出
     */
    public static Field getField(Class<?> clazz, String fieldName, boolean getFieldOrDeclared, boolean ignoreIfNotFound) {
        AssertTool.notNull(clazz, "Class must not be null");
        AssertTool.notNull(fieldName, "Field name must not be null");

        try {
            return getFieldOrDeclared
                    ? clazz.getField(fieldName)
                    : clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            if (ignoreIfNotFound) {
                return null;
            }
            throw new IllegalStateException("Expected field not found: " + e);
        }
    }

    /**
     * 获取字段数组。
     *
     * <p>{@code getFieldOrDeclared} 为 {@code true} 时使用 {@link Class#getFields()}，返回当前类及父类的 public 字段；
     * 为 {@code false} 时使用 {@link Class#getDeclaredFields()}，仅返回当前类声明的字段（任意访问级别，不含父类字段）。
     *
     * @param clazz              目标类，不可为 {@code null}
     * @param getFieldOrDeclared {@code true} 取 public 字段（含继承），{@code false} 取当前类声明的字段
     * @return 字段数组，无字段时返回空数组
     * @throws IllegalArgumentException {@code clazz} 为 {@code null} 时抛出
     */
    public static Field[] getFields(Class<?> clazz, boolean getFieldOrDeclared) {
        AssertTool.notNull(clazz, "Class must not be null");
        return getFieldOrDeclared
                ? clazz.getFields()
                : clazz.getDeclaredFields();
    }

    /**
     * 判断字段是否同时具备 public、static、final 修饰符（常量字段）。
     *
     * @param field 待判断的字段
     * @return 同时为 public、static、final 时返回 {@code true}
     * @throws NullPointerException {@code field} 为 {@code null} 时抛出
     */
    public static boolean isPublicStaticFinal(Field field) {
        int modifiers = field.getModifiers();
        return (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers));
    }


    // ---------------------- field operate ----------------------

    /**
     * 打开字段访问权限：调用 {@link java.lang.reflect.AccessibleObject#trySetAccessible()} 尝试开启访问。
     *
     * <p>与 {@link #makeAccessible(Method)} 不同，这里不判断字段修饰符，任何字段都会尝试打开；
     * 会修改 {@link Field} 实例的 accessible 标记（对同一实例全局可见）。受模块强封装限制、无法开启访问时，
     * 抛出 {@link java.lang.reflect.InaccessibleObjectException}。
     *
     * @param field 待处理的字段，不可为 {@code null}
     * @throws java.lang.reflect.InaccessibleObjectException 无法开启访问权限时抛出
     */
    public static void makeAccessible(Field field) {
        if (!field.trySetAccessible()) {
            throw new InaccessibleObjectException("Unable to make field accessible: " + field);
        }
    }

    /**
     * 读取字段值。
     *
     * <p>读取前会调用 {@link #makeAccessible(Field)} 打开访问权限，因此可读取 private 字段；
     * 读取 static 字段时 {@code target} 可为 {@code null}。
     *
     * @param field  待读取的字段，不可为 {@code null}
     * @param target 目标对象；字段为 static 时可为 {@code null}
     * @return 字段值；基本类型字段返回对应包装类型
     * @throws RuntimeException 访问失败时抛出（包装 {@link IllegalAccessException}）
     */
    public static Object getFieldValue(Field field, Object target) {
        makeAccessible(field);
        try {
            return field.get(target);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException("Failed to get value from field [" + field + "]", ex);
        }
    }

    /**
     * 设置字段值。
     *
     * <p>写入前会调用 {@link #makeAccessible(Field)} 打开访问权限；写入 static 字段时 {@code target} 可为 {@code null}。
     * 写入 final 字段受 JDK 版本限制，可能失败；写入 {@code null} 到基本类型字段会失败。
     *
     * @param field  待写入的字段，不可为 {@code null}
     * @param target 目标对象；字段为 static 时可为 {@code null}
     * @param value  写入值，可为 {@code null}
     * @throws RuntimeException 访问失败、字段为 final 不可写或类型不匹配时抛出
     */
    public static void setFieldValue(Field field, Object target, Object value) {
        makeAccessible(field);
        try {
            field.set(target, value);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException("Failed to set value to field [" + field + "]", ex);
        }
    }


    // ---------------------- field process ----------------------

    /**
     * 按继承层次遍历字段：从 {@code clazz} 开始，先处理当前类声明的字段（{@link Class#getDeclaredFields()}），
     * 再上溯父类，直到（不含）{@link Object}。
     *
     * <p>子类字段先于父类字段被回调；不做同名去重，父类的同名字段同样会被回调。
     *
     * @param clazz         起始类，不可为 {@code null}
     * @param fieldCallback 每个字段的处理回调，不可为 {@code null}
     * @throws IllegalArgumentException {@code clazz} 为 {@code null} 时抛出
     * @throws IllegalStateException    回调抛出 {@link IllegalAccessException} 时抛出
     */
    public static void doWithFields(Class<?> clazz, FieldCallback fieldCallback) {
        // Keep backing up the inheritance hierarchy.
        Class<?> targetClass = clazz;
        do {
            Field[] fields = getFields(targetClass, false);
            for (Field field : fields) {
                try {
                    fieldCallback.doWith(field);
                } catch (IllegalAccessException ex) {
                    throw new IllegalStateException("Not allowed to access field '" + field.getName() + "': " + ex);
                }
            }
            targetClass = targetClass.getSuperclass();
        } while (targetClass != null && targetClass != Object.class);
    }

    /**
     * 字段回调接口：{@link #doWithFields(Class, FieldCallback)} 遍历继承层次时，对每个字段调用一次。
     */
    public interface FieldCallback {

        /**
         * 处理单个字段。
         *
         * @param field 待处理的字段
         * @throws IllegalArgumentException 回调实现判定参数非法时抛出
         * @throws IllegalAccessException   字段不可访问时抛出，由 {@link #doWithFields} 转换为 {@link IllegalStateException}
         */
        void doWith(Field field) throws IllegalArgumentException, IllegalAccessException;
    }


    // ---------------------- Proxy ----------------------

    /**
     * 创建实现指定接口的 JDK 动态代理实例。
     *
     * <p>代理类由 {@link Proxy} 生成，仅实现 {@code interfaceType} 一个接口，并使用该接口的类加载器；
     * 对代理对象的所有方法调用都会转发给 {@code handler}。
     *
     * @param interfaceType 需要实现的接口类型，不可为 {@code null} 且必须是接口
     * @param handler       方法调用处理器，不可为 {@code null}
     * @param <T>           代理类型
     * @return 代理实例
     * @throws IllegalArgumentException 参数为 {@code null} 或 {@code interfaceType} 不是接口时抛出
     */
    public static <T> T newProxy(Class<T> interfaceType, InvocationHandler handler) {
        AssertTool.notNull(interfaceType, "Interface type must not be null");
        AssertTool.notNull(handler, "InvocationHandler must not be null");
        AssertTool.isTrue(interfaceType.isInterface(), interfaceType + "is not an interface");
        Object object = Proxy.newProxyInstance(
                interfaceType.getClassLoader(),
                new Class<?>[]{
                        interfaceType
                },
                handler
        );
        return interfaceType.cast(object);
    }

}
