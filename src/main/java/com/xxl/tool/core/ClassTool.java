package com.xxl.tool.core;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Class Tool / Class 工具：提供类信息读取、类型可赋值判断、类名解析与基本类型判断能力。
 *
 * <p>覆盖范围：
 * <ul>
 *   <li>获取对象/类的类型、类名与包名（{@link #getClass(Object)}、{@link #getClassName(Object, boolean)}、{@link #getPackageName(Class)}）；</li>
 *   <li>父子类型、基本类型与包装类型之间的可赋值判断（{@link #isAssignable(Class, Class)}）；</li>
 *   <li>按名称解析类，支持 {@code "int"} 等基本类型名称（{@link #resolveClass(String)}）；</li>
 *   <li>基本类型、包装类型及 {@link String} 的类型判断。</li>
 * </ul>
 *
 * <p>所有方法均为静态方法；内部类型映射表在静态初始化后只读，类无状态、线程安全。
 *
 * @author xuxueli 2024-01-21 05:03:10
 */
public class ClassTool {

    // ---------------------- class info ----------------------

    /**
     * 获取对象的运行时类型。
     *
     * @param obj 目标对象，可为 {@code null}
     * @param <T> 对象类型
     * @return 对象的运行时 {@link Class}；{@code obj} 为 {@code null} 时返回 {@code null}
     */
    public static <T> Class<T> getClass(T obj) {
        return ((null == obj) ? null : (Class<T>) obj.getClass());
    }

    /**
     * 获取对象的类名。
     *
     * @param obj      目标对象，可为 {@code null}
     * @param isSimple {@code true} 返回简单类名，{@code false} 返回全限定类名
     * @return 类名；{@code obj} 为 {@code null} 时返回 {@code null}；匿名类/局部类的简单类名可能为空串
     */
    public static String getClassName(Object obj, boolean isSimple) {
        if (null == obj) {
            return null;
        }
        final Class<?> clazz = obj.getClass();
        return getClassName(clazz, isSimple);
    }

    /**
     * 获取类的类名。
     *
     * @param clazz    目标类，可为 {@code null}
     * @param isSimple {@code true} 返回简单类名，{@code false} 返回全限定类名
     * @return 类名；{@code clazz} 为 {@code null} 时返回 {@code null}；匿名类/局部类的简单类名可能为空串
     */
    public static String getClassName(Class<?> clazz, boolean isSimple) {
        if (null == clazz) {
            return null;
        }
        return isSimple ? clazz.getSimpleName() : clazz.getName();
    }

    /**
     * 获取类所在包名：内部按 {@link Class#getName()} 的最后一个 {@code '.'} 截取。
     *
     * <p>按类名文本截取，不特殊处理数组与内部类，例如 {@code String[].class} 会得到 {@code "[Ljava.lang"}；
     * 默认包（无名包）返回空串。
     *
     * @param clazz 目标类，可为 {@code null}
     * @return 包名；{@code clazz} 为 {@code null} 时返回 {@code null}；无包名时返回 {@code ""}
     */
    public static String getPackageName(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        return getPackageName(clazz.getName());
    }

    /**
     * 获取类全名对应的包名。
     *
     * @param classFullName 类全名，可为 {@code null}
     * @return 包名；{@code classFullName} 为 {@code null} 时返回 {@code null}；不含 {@code '.'} 时返回 {@code ""}
     */
    public static String getPackageName(String classFullName) {
        if (classFullName == null) {
            return null;
        }
        int lastDot = classFullName.lastIndexOf('.');
        return (lastDot < 0) ? "" : classFullName.substring(0, lastDot);
    }


    // ---------------------- class is assignable ----------------------

    /**
     * 包装类型 → 基本类型映射（{@link IdentityHashMap}，按引用比较）；静态初始化后只读，线程安全。
     */
    private static final Map<Class<?>, Class<?>> primitiveWrapper2TypeMap = new IdentityHashMap<>(9);
    /**
     * 基本类型 → 包装类型映射（{@link IdentityHashMap}，按引用比较）；静态初始化后只读，线程安全。
     */
    private static final Map<Class<?>, Class<?>> primitiveType2WrapperMap = new IdentityHashMap<>(9);
    static {
        // Integer -> int
        primitiveWrapper2TypeMap.put(Boolean.class, boolean.class);
        primitiveWrapper2TypeMap.put(Byte.class, byte.class);
        primitiveWrapper2TypeMap.put(Character.class, char.class);
        primitiveWrapper2TypeMap.put(Double.class, double.class);
        primitiveWrapper2TypeMap.put(Float.class, float.class);
        primitiveWrapper2TypeMap.put(Integer.class, int.class);
        primitiveWrapper2TypeMap.put(Long.class, long.class);
        primitiveWrapper2TypeMap.put(Short.class, short.class);
        primitiveWrapper2TypeMap.put(Void.class, void.class);

        // int -> Integer
        for (Map.Entry<Class<?>, Class<?>> entry : primitiveWrapper2TypeMap.entrySet()) {
            primitiveType2WrapperMap.put(entry.getValue(), entry.getKey());
        }
    }

    /**
     * 判断右值类型能否赋值给左值类型，兼容基本类型与其包装类型。
     *
     * <p>判定规则：
     * <ul>
     *   <li>任一参数为 {@code null} 时返回 {@code false}；</li>
     *   <li>{@link Class#isAssignableFrom(Class)} 为 {@code true} 时返回 {@code true}（同类型或父子关系）；</li>
     *   <li>左值为基本类型时，右值允许为对应包装类型；左值为引用/包装类型时，右值允许为对应基本类型；</li>
     *   <li>不做数值拓宽转换，例如 {@code int} 不可赋值给 {@code long}。</li>
     * </ul>
     *
     * 1、Considers primitive wrapper classes as assignable to the corresponding primitive types.
     *
     * <pre>
     *     ClassTool.isAssignable(Number.class, Integer.class);         : true
     *     ClassTool.isAssignable(Integer.class, Integer.class);        : true
     *     ClassTool.isAssignable(List.class, ArrayList.class);         : true
     *     ClassTool.isAssignable(Object.class, String.class);          : true
     * </pre>
     *
     * @param leftHandType  赋值目标类型（左值），可为 {@code null}
     * @param rightHandType 赋值来源类型（右值），可为 {@code null}
     * @return 右值类型可赋值给左值类型时返回 {@code true}；任一参数为 {@code null} 时返回 {@code false}
     */
    public static boolean isAssignable(Class<?> leftHandType, Class<?> rightHandType) {
        if (rightHandType == null || leftHandType == null) {
            return false;
        }

        /**
         * // Sub 是否可以赋值给 Parent： 相等，或者 Parent 是其父类；
         * List -> ArrayList: true
         */
        if (leftHandType.isAssignableFrom(rightHandType)) {
            return true;
        }

        // primitive type
        if (leftHandType.isPrimitive()) {
            Class<?> resolvedPrimitive = primitiveWrapper2TypeMap.get(rightHandType);
            return (leftHandType == resolvedPrimitive);
        } else {
            Class<?> resolvedWrapper = primitiveType2WrapperMap.get(rightHandType);
            return (resolvedWrapper != null && leftHandType.isAssignableFrom(resolvedWrapper));
        }
    }

    // ---------------------- resolve class ----------------------

    /**
     * 基本类型名称 → 基本类型映射（如 {@code "int" → int.class}）；静态初始化后只读，线程安全。
     */
    private static final HashMap<String, Class<?>> primitiveString2TypMap = new HashMap<>();
    static {
        // "int" -> int.class
        primitiveString2TypMap.put("boolean", boolean.class);
        primitiveString2TypMap.put("byte", byte.class);
        primitiveString2TypMap.put("char", char.class);
        primitiveString2TypMap.put("short", short.class);
        primitiveString2TypMap.put("int", int.class);
        primitiveString2TypMap.put("long", long.class);
        primitiveString2TypMap.put("float", float.class);
        primitiveString2TypMap.put("double", double.class);
        primitiveString2TypMap.put("void", void.class);
    }

    /**
     * 按类名解析 {@link Class}。
     *
     * <p>优先使用 {@link Class#forName(String)} 加载（会触发类的静态初始化）；加载失败时回退到基本类型名称映射，
     * 支持 {@code "int"}、{@code "boolean"}、{@code "void"} 等。数组名称需按 {@link Class#forName(String)} 要求的格式传入。
     *
     * @param className 类名（全限定名或基本类型名），不可为 {@code null}
     * @return 解析后的 {@link Class}
     * @throws ClassNotFoundException 名称既不是可加载的类，也不是受支持的基本类型名时抛出
     */
    public static Class<?> resolveClass(String className) throws ClassNotFoundException {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException ex) {
            Class<?> cl = primitiveString2TypMap.get(className);
            if (cl != null) {
                return cl;
            } else {
                throw ex;
            }
        }
    }


    // ---------------------- is primitive ----------------------

    /**
     * 判断是否为基本类型（含 {@code void}，不含包装类型）。
     *
     * @param clazz 待判断的类型，不可为 {@code null}
     * @return 基本类型返回 {@code true}
     * @throws IllegalArgumentException {@code clazz} 为 {@code null} 时抛出
     */
    public static boolean isPrimitive(Class<?> clazz) {
        AssertTool.notNull(clazz, "Class must not be null");
        return clazz.isPrimitive();
    }

    /**
     * 判断是否为基本类型的包装类型（如 {@link Integer}、{@link Void}）。
     *
     * @param clazz 待判断的类型，不可为 {@code null}
     * @return 包装类型返回 {@code true}
     * @throws IllegalArgumentException {@code clazz} 为 {@code null} 时抛出
     */
    public static boolean isPrimitiveWrapper(Class<?> clazz) {
        AssertTool.notNull(clazz, "Class must not be null");
        return primitiveWrapper2TypeMap.containsKey(clazz);
    }

    /**
     * 判断是否为基本类型或其包装类型。
     *
     * @param clazz 待判断的类型，不可为 {@code null}
     * @return 基本类型或包装类型返回 {@code true}
     * @throws IllegalArgumentException {@code clazz} 为 {@code null} 时抛出
     */
    public static boolean isPrimitiveOrWrapper(Class<?> clazz) {
        AssertTool.notNull(clazz, "Class must not be null");
        return isPrimitive(clazz) || isPrimitiveWrapper(clazz);
    }

    /**
     * 判断是否为基本类型、其包装类型或 {@link String}。
     *
     * @param clazz 待判断的类型，不可为 {@code null}
     * @return 基本类型、包装类型或 {@code String} 返回 {@code true}
     * @throws IllegalArgumentException {@code clazz} 为 {@code null} 时抛出
     */
    public static boolean isPrimitiveOrWrapperOrString(Class<?> clazz) {
        return isPrimitiveOrWrapper(clazz) || String.class.equals(clazz);
    }


    // ---------------------- other ----------------------


}
