package com.xxl.tool.core;

import java.lang.reflect.*;

/**
 * Reflection Tool
 *
 * @author xuxueli 2024-01-21 05:03:10
 */
public class ReflectionTool {


    // ---------------------- find method ----------------------

    /**
     * get method
     *
     * @param clazz         the clazz to analyze
     * @param methodName    the name of the method
     * @param paramTypes    the parameter types of the method (can be {@code null} to indicate any signature)
     * @return              the method, or {@code null} if not found
     */
    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        return getMethod(clazz, methodName, paramTypes, true, true);
    }

    /**
     * get declared method
     *
     * @param clazz         the clazz to analyze
     * @param methodName    the name of the method
     * @param paramTypes    the parameter types of the method (can be {@code null} to indicate any signature)
     * @return              the method, or {@code null} if not found
     */
    public static Method getDeclaredMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        return getMethod(clazz, methodName, paramTypes, false, true);
    }

    /**
     * get method
     *
     * @param clazz                 class
     * @param methodName            method name
     * @param paramTypes            parameter types
     * @param getMethodOrDeclared   true means getMethod, false means getDeclaredMethod
     * @param ignoreIfNotFound      true means ignore if not found, false means throw exception if not found
     * @return specified method
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
     * get all methods
     *
     * @param clazz  class
     * @param getMethodOrDeclared true means getMethods, false means getDeclaredMethods
     * @return all methods
     */
    public static Method[] getAllMethods(Class<?> clazz, boolean getMethodOrDeclared) {
        return getMethodOrDeclared
                ? clazz.getMethods()
                : clazz.getDeclaredMethods();
    }


    // ---------------------- method operate ----------------------

    /**
     * make method accessible
     *
     * @param method    the method to make accessible
     */
    public static void makeAccessible(Method method) {
        if ((!Modifier.isPublic(method.getModifiers())
                || !Modifier.isPublic(method.getDeclaringClass().getModifiers())
            ) && !method.isAccessible()) {
            method.setAccessible(true);
        }
    }

    /**
     * invoke method
     *
     * @param method    method to invoke
     * @param target    target object to invoke
     * @param args      arguments to pass into method
     * @return          method invoke result
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
     * get field
     */
    public static Field getField(Class<?> clazz, String fieldName) {
        return getField(clazz, fieldName, true, true);
    }

    /**
     * get declared field
     */
    public static Field getDeclaredField(Class<?> clazz, String fieldName) {
        return getField(clazz, fieldName, false, true);
    }

    /**
     * find field
     *
     * @param clazz  class
     * @param fieldName  field name
     * @param getFieldOrDeclared true means getField, false means getDeclaredField
     * @param ignoreIfNotFound true means ignore if not found, false means throw exception if not found
     * @return specified field
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
     * get all fields, contains current and parent class fields
     *
     * @param clazz     class to find fields
     * @param getFieldOrDeclared true use getFields, false use getDeclaredFields
     * @return all fields
     */
    public static Field[] getAllFields(Class<?> clazz, boolean getFieldOrDeclared) {
        AssertTool.notNull(clazz, "Class must not be null");
        return getFieldOrDeclared
                ? clazz.getFields()
                : clazz.getDeclaredFields();
    }

    /**
     * check field is public static final
     *
     * @param field     field to check
     * @return          true if field is public static final
     */
    public static boolean isPublicStaticFinal(Field field) {
        int modifiers = field.getModifiers();
        return (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers));
    }


    // ---------------------- field operate ----------------------

    /**
     * get field value
     *
     * @param field     field to get
     * @param target    target object to get
     * @return          field value
     */
    public static Object getFieldValue(Field field, Object target) {
        try {
            return field.get(target);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException("Failed to get value from field [" + field + "]", ex);
        }
    }

    /**
     * make field accessible
     *
     * @param field  field to make accessible
     */
    public static void makeAccessible(Field field) {
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
    }

    /**
     * set field value
     *
     * @param field     field to set
     * @param target    target object to set
     * @param value     value to set
     */
    public static void setField(Field field, Object target, Object value) {
        try {
            field.set(target, value);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException("Failed to set value to field [" + field + "]", ex);
        }
    }


    // ---------------------- field process ----------------------

    /**
     * iterate processing fields
     *
     * @param clazz     class to introspect
     * @param fieldCallback        callback to invoke for each field
     */
    public static void doWithFields(Class<?> clazz, FieldCallback fieldCallback) {
        // Keep backing up the inheritance hierarchy.
        Class<?> targetClass = clazz;
        do {
            Field[] fields = getAllFields(targetClass, false);
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
     * Callback interface invoked on each field in the hierarchy.
     */
    public interface FieldCallback {

        /**
         * Perform an operation using the given field.
         * @param field the field to operate on
         */
        void doWith(Field field) throws IllegalArgumentException, IllegalAccessException;
    }


    // ---------------------- Proxy ----------------------

    /**
     * return a proxy instance that implements the specified interfaceType
     *
     * @param interfaceType     interfaceType to implement
     * @param handler           handler to handle method invocation
     * @return                  a proxy instance
     * @param <T>               the type of the proxy
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
