package com.xxl.tool.core;

import java.lang.reflect.Method;
import java.util.*;

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

    // ---------------------- class is assignable ----------------------

    private static final Map<Class<?>, Class<?>> primitiveWrapper2TypeMap = new IdentityHashMap<>(9);
    private static final Map<Class<?>, Class<?>> primitiveType2WrapperMap = new IdentityHashMap<>(9);
    static {
        primitiveWrapper2TypeMap.put(Boolean.class, boolean.class);
        primitiveWrapper2TypeMap.put(Byte.class, byte.class);
        primitiveWrapper2TypeMap.put(Character.class, char.class);
        primitiveWrapper2TypeMap.put(Double.class, double.class);
        primitiveWrapper2TypeMap.put(Float.class, float.class);
        primitiveWrapper2TypeMap.put(Integer.class, int.class);
        primitiveWrapper2TypeMap.put(Long.class, long.class);
        primitiveWrapper2TypeMap.put(Short.class, short.class);
        primitiveWrapper2TypeMap.put(Void.class, void.class);

        // Map entry iteration is less expensive to initialize than forEach with lambdas
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
     *     ClassTool.isAssignable(Number.class, Integer.class)
     * </pre>
     *
     * @param parentType   the target type
     * @param subType      the value type
     * @return  {@code true} if {@code subType} is assignable to {@code parentType}
     */
    public static boolean isAssignable(Class<?> parentType, Class<?> subType) {
        if (subType == null || parentType == null) {
            return false;
        }

        // object type
        if (parentType.isAssignableFrom(subType)) {
            return true;
        }

        // primitive type
        if (parentType.isPrimitive()) {
            Class<?> resolvedPrimitive = primitiveWrapper2TypeMap.get(subType);
            return (parentType == resolvedPrimitive);
        } else {
            Class<?> resolvedWrapper = primitiveType2WrapperMap.get(subType);
            return (resolvedWrapper != null && parentType.isAssignableFrom(resolvedWrapper));
        }
    }

    // ---------------------- resolve class ----------------------

    private static final HashMap<String, Class<?>> primitiveString2TypMap = new HashMap<>();
    static {
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

    // ---------------------- find method ----------------------

    /**
     * get method with method name and parameter types
     *
     * <pre>
     *     Method method = ClassTool.getMethod(MyBean.class, "myMethod", new Class<?>[]{String.class});
     * </pre>
     *
     * @param clazz         the clazz to analyze
     * @param methodName    the name of the method
     * @param paramTypes    the parameter types of the method (can be {@code null} to indicate any signature)
     * @return              the method, or {@code null} if not found
     */
    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        AssertTool.notNull(clazz, "Class must not be null");
        AssertTool.notNull(methodName, "Method name must not be null");

        if (paramTypes != null) {
            try {
                return clazz.getMethod(methodName, paramTypes);
            } catch (NoSuchMethodException ex) {
                throw new IllegalStateException("Expected method not found: " + ex);
            }
        } else {
            Set<Method> candidates = findMethodCandidatesByName(clazz, methodName);
            if (candidates.size() == 1) {
                return candidates.iterator().next();
            } else if (candidates.isEmpty()) {
                throw new IllegalStateException("Expected method not found: " + clazz.getName() + '.' + methodName);
            } else {
                throw new IllegalStateException("No unique method found: " + clazz.getName() + '.' + methodName);
            }
        }
    }

    /**
     * get method with method name and parameter types
     *
     * <pre>
     *     Method method = ClassTool.getMethodIfAvailable(MyBean.class, "myMethod", new Class<?>[]{String.class});
     * </pre>
     *
     * @param clazz         class to analyze
     * @param methodName    method name
     * @param paramTypes    parameter types
     * @return method or null; return null if not found, return first one if multiple
     */
    public static Method getMethodIfAvailable(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        AssertTool.notNull(clazz, "Class must not be null");
        AssertTool.notNull(methodName, "Method name must not be null");

        if (paramTypes != null) {
            return getMethodOrNull(clazz, methodName, paramTypes);
        } else {
            Set<Method> candidates = findMethodCandidatesByName(clazz, methodName);
            if (candidates.size() == 1) {
                return candidates.iterator().next();
            }
            return null;
        }
    }

    /**
     * get method with method name and parameter types, return null if not found
     */
    private static Method getMethodOrNull(Class<?> clazz, String methodName, Class<?>[] paramTypes) {
        try {
            return clazz.getMethod(methodName, paramTypes);
        } catch (NoSuchMethodException ex) {
            return null;
        }
    }

    /**
     * find method candidates by method name
     */
    private static Set<Method> findMethodCandidatesByName(Class<?> clazz, String methodName) {
        Set<Method> candidates = new HashSet<>(1);
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            if (methodName.equals(method.getName())) {
                candidates.add(method);
            }
        }
        return candidates;
    }

    // ---------------------- primitive ----------------------

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
