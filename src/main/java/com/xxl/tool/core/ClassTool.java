package com.xxl.tool.core;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Class Tool
 *
 * @author xuxueli 2024-01-21 05:03:10
 */
public class ClassTool {

    // ---------------------- class info ----------------------

    /**
     * get class
     *
     * @param obj  object
     * @return  class
     */
    public static <T> Class<T> getClass(T obj) {
        return ((null == obj) ? null : (Class<T>) obj.getClass());
    }

    /**
     * get class name
     *
     * @param obj   object
     * @param isSimple true means simple class name, false means full class name
     * @return class name
     */
    public static String getClassName(Object obj, boolean isSimple) {
        if (null == obj) {
            return null;
        }
        final Class<?> clazz = obj.getClass();
        return getClassName(clazz, isSimple);
    }

    /**
     * get class name
     *
     * @param clazz  class
     * @param isSimple true means simple class name, false means full class name
     * @return class name
     */
    public static String getClassName(Class<?> clazz, boolean isSimple) {
        if (null == clazz) {
            return null;
        }
        return isSimple ? clazz.getSimpleName() : clazz.getName();
    }

    /**
     * get package name
     *
     * @param clazz     the class to introspect
     * @return          package name of {@code clazz}
     */
    public static String getPackageName(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        return getPackageName(clazz.getName());
    }

    /**
     * get package name
     *
     * @param classFullName     the class to introspect
     * @return                  package name of {@code classFullName}
     */
    public static String getPackageName(String classFullName) {
        if (classFullName == null) {
            return null;
        }
        int lastDot = classFullName.lastIndexOf('.');
        return (lastDot < 0) ? "" : classFullName.substring(0, lastDot);
    }


    // ---------------------- class is assignable ----------------------

    private static final Map<Class<?>, Class<?>> primitiveWrapper2TypeMap = new IdentityHashMap<>(9);
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
     * whether the sub-type can be assigned to the parent-type
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
     * @param leftHandType          left-hand type
     * @param rightHandType         right-hand type
     * @return  true if rightHandType can be assigned to leftHandType
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
     * resolve class by name
     *
     * @param className the class name
     * @return the resolved class
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
     * whether is a primitive type
     *
     * @param clazz the class to check
     * @return true if the specified class is a primitive type
     */
    public static boolean isPrimitive(Class<?> clazz) {
        AssertTool.notNull(clazz, "Class must not be null");
        return clazz.isPrimitive();
    }

    /**
     * whether is a primitive wrapper type
     *
     * @param clazz the class to check
     * @return true if the specified class is a primitive wrapper type
     */
    public static boolean isPrimitiveWrapper(Class<?> clazz) {
        AssertTool.notNull(clazz, "Class must not be null");
        return primitiveWrapper2TypeMap.containsKey(clazz);
    }

    /**
     * whether is a primitive or wrapper type
     *
     * @param clazz the class to check
     * @return true if the specified class is a primitive or wrapper type
     */
    public static boolean isPrimitiveOrWrapper(Class<?> clazz) {
        AssertTool.notNull(clazz, "Class must not be null");
        return isPrimitive(clazz) || isPrimitiveWrapper(clazz);
    }

    /**
     * whether is a primitive or wrapper type or String
     *
      * @param clazz the class to check
     * @return true if the specified class is a primitive or wrapper type or String
     */
    public static boolean isPrimitiveOrWrapperOrString(Class<?> clazz) {
        return isPrimitiveOrWrapper(clazz) || String.class.equals(clazz);
    }


    // ---------------------- other ----------------------


}
