package com.xxl.tool.core;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Reflection Tool
 *
 * @author xuxueli 2024-01-21 05:03:10
 */
public class ReflectionTool {

    // ---------------------- Info ----------------------

    /**
     * Returns the package name of {@code clazz}
     * @param clazz     the class to introspect
     * @return          package name of {@code clazz}
     */
    public static String getPackageName(Class<?> clazz) {
        return getPackageName(clazz.getName());
    }


    /**
     * Returns the package name of {@code classFullName}
     *
     * @param classFullName     the class to introspect
     * @return                  package name of {@code classFullName}
     */
    public static String getPackageName(String classFullName) {
        int lastDot = classFullName.lastIndexOf('.');
        return (lastDot < 0) ? "" : classFullName.substring(0, lastDot);
    }

    // ---------------------- Method ----------------------

    /**
     * find method of class by method name and params
     *
     * @param clazz     the class to introspect
     * @param name      the name of the method
     * @return          the Method object, or {@code null} if none found
     */
    public static Method findMethod(Class<?> clazz, String name) {
        return findMethod(clazz, name, null);
    }

    /**
     * find method of class by method method name and params
     *
     * @param clazz         the class to introspect
     * @param name          the name of the method
     * @param paramTypes    the parameter types of the method
     * @return              the Method object, or {@code null} if none found
     */
    public static Method findMethod(Class<?> clazz, String name, Class<?>... paramTypes) {
        AssertTool.notNull(clazz, "Class must not be null");
        AssertTool.notNull(name, "Method name must not be null");
        Class<?> searchType = clazz;
        while (searchType != null) {
            Method[] methods = (searchType.isInterface()
                    ? searchType.getMethods() :
                    getDeclaredMethods(searchType, false));
            for (Method method : methods) {
                if (name.equals(method.getName()) && (paramTypes == null || hasSameParams(method, paramTypes))) {
                    return method;
                }
            }
            searchType = searchType.getSuperclass();
        }
        return null;
    }

    /**
     * check if method has same params
     */
    private static boolean hasSameParams(Method method, Class<?>[] paramTypes) {
        return (paramTypes.length == method.getParameterCount() &&
                Arrays.equals(paramTypes, method.getParameterTypes()));
    }

    /**
     * get declared methods
     */
    private static Method[] getDeclaredMethods(Class<?> clazz, boolean defensive) {
        AssertTool.notNull(clazz, "Class must not be null");
        Method[] result;
        try {
            Method[] declaredMethods = clazz.getDeclaredMethods();
            List<Method> defaultMethods = findConcreteMethodsOnInterfaces(clazz);
            if (defaultMethods != null) {
                result = new Method[declaredMethods.length + defaultMethods.size()];
                System.arraycopy(declaredMethods, 0, result, 0, declaredMethods.length);
                int index = declaredMethods.length;
                for (Method defaultMethod : defaultMethods) {
                    result[index] = defaultMethod;
                    index++;
                }
            }
            else {
                result = declaredMethods;
            }
        } catch (Throwable ex) {
            throw new IllegalStateException("Failed to introspect Class [" + clazz.getName() +
                    "] from ClassLoader [" + clazz.getClassLoader() + "]", ex);
        }
        return (result.length == 0 || !defensive) ? result : result.clone();
    }

    /**
     * find concrete（default） methods on interfaces
     */
    private static List<Method> findConcreteMethodsOnInterfaces(Class<?> clazz) {
        List<Method> result = null;
        for (Class<?> ifc : clazz.getInterfaces()) {
            for (Method ifcMethod : ifc.getMethods()) {
                if (!Modifier.isAbstract(ifcMethod.getModifiers())) {
                    if (result == null) {
                        result = new ArrayList<>();
                    }
                    result.add(ifcMethod);
                }
            }
        }
        return result;
    }

    /**
     * make method accessible
     *
     * @param method    the method to make accessible
     */
    public static void makeAccessible(Method method) {
        if ((!Modifier.isPublic(method.getModifiers()) || !Modifier.isPublic(method.getDeclaringClass().getModifiers()))
                && !method.isAccessible()) {
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


    // ---------------------- Field ----------------------

    /**
     * find field of class by field name
     *
     * @param clazz     class to introspect
     * @param name      name of the field
     * @return          field object, or {@code null} if none found
     */
    public static Field findField(Class<?> clazz, String name) {
        return findField(clazz, name, null);
    }

    /**
     * find field of class by field name and field type
     *
     * @param clazz     class to introspect
     * @param name      name of the field
     * @param type      type of the field
     * @return          field object, or {@code null} if none found
     */
    public static Field findField(Class<?> clazz, String name, Class<?> type) {
        AssertTool.notNull(clazz, "Class must not be null");
        AssertTool.isTrue(name != null || type != null, "Either name or type of the field must be specified");
        Class<?> searchType = clazz;
        while (Object.class != searchType && searchType != null) {
            Field[] fields = getDeclaredFields(searchType);
            for (Field field : fields) {
                if ((name == null || name.equals(field.getName())) &&
                        (type == null || type.equals(field.getType()))) {
                    return field;
                }
            }
            searchType = searchType.getSuperclass();
        }
        return null;
    }

    /**
     * find all fields on the given class and superclasses
     *
     * @param clazz     the class to introspect
     * @return          fields
     */
    private static Field[] getDeclaredFields(Class<?> clazz) {
        AssertTool.notNull(clazz, "Class must not be null");
        Field[] result;
        try {
            result = clazz.getDeclaredFields();
        } catch (Throwable ex) {
            throw new IllegalStateException("Failed to introspect Class [" + clazz.getName() + "] from ClassLoader [" + clazz.getClassLoader() + "]", ex);
        }
        return result;
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

    /**
     * get field value
     *
     * @param field     field to get
     * @param target    target object to get
     * @return          field value
     */
    public static Object getField(Field field, Object target) {
        try {
            return field.get(target);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException("Failed to get value from field [" + field + "]", ex);
        }
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

    /**
     * iterate processing fields
     *
     * @param clazz     class to introspect
     * @param fc        callback to invoke for each field
     */
    public static void doWithFields(Class<?> clazz, FieldCallback fc) {
        // Keep backing up the inheritance hierarchy.
        Class<?> targetClass = clazz;
        do {
            Field[] fields = getDeclaredFields(targetClass);
            for (Field field : fields) {
                try {
                    fc.doWith(field);
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
