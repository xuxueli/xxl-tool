package com.xxl.tool.core;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Class Tool
 *
 * @author xuxueli 2024-01-21 05:03:10
 */
public class ClassTool {


    // ---------------------- judge ----------------------

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
     * Check if the super-type may be assigned to the sub-type
     * Considers primitive wrapper classes as assignable to the corresponding primitive types.
     *
     * @param superType   the target type (left-hand side (LHS) type)
     * @param subType   the value type (right-hand side (RHS) type) that should be assigned to the target type
     * @return  {@code true} if {@code rhsType} is assignable to {@code lhsType}
     */
    public static boolean isAssignable(Class<?> superType, Class<?> subType) {
        AssertTool.notNull(superType, "Left-hand side type must not be null");
        AssertTool.notNull(subType, "Right-hand side type must not be null");
        if (superType.isAssignableFrom(subType)) {
            return true;
        }
        if (superType.isPrimitive()) {
            Class<?> resolvedPrimitive = primitiveWrapper2TypeMap.get(subType);
            return (superType == resolvedPrimitive);
        } else {
            Class<?> resolvedWrapper = primitiveType2WrapperMap.get(subType);
            return (resolvedWrapper != null && superType.isAssignableFrom(resolvedWrapper));
        }
    }

    /**
     * Determine whether the given class has a public method with the given signature, and return it if available (else return {@code null}).
     *
     * <p>In case of any signature specified, only returns the method if there is a
     * unique candidate, i.e. a single public method with the specified name.
     * <p>Essentially translates {@code NoSuchMethodException} to {@code null}.
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
            return getMethodOrNull(clazz, methodName, paramTypes);
        } else {
            Set<Method> candidates = findMethodCandidatesByName(clazz, methodName);
            if (candidates.size() == 1) {
                return candidates.iterator().next();
            }
            return null;
        }
    }

    private static Method getMethodOrNull(Class<?> clazz, String methodName, Class<?>[] paramTypes) {
        try {
            return clazz.getMethod(methodName, paramTypes);
        }
        catch (NoSuchMethodException ex) {
            return null;
        }
    }

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

}
