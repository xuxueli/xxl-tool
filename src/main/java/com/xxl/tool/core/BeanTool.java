package com.xxl.tool.core;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
/**
 * @Author zhanlixiang
 * @Description Bean工具类 - 实现Bean属性拷贝功能
 * @Date 2025/11/11
 */
public class BeanTool {

    private static final Map<Class<?>, Map<String, Field>> FIELD_CACHE = new ConcurrentHashMap<>();

    private BeanTool() {
        // 工具类，防止实例化
    }

    /**
     * 拷贝源对象属性到目标对象（浅拷贝）
     * @param source 源对象
     * @param target 目标对象
     * @param <T> 目标类型
     * @return 目标对象
     */
    public static <T> T copyProperties(Object source, T target) {
        return copyProperties(source, target, false, (String[]) null);
    }

    /**
     * 拷贝源对象属性到目标对象，可忽略某些属性
     * @param source 源对象
     * @param target 目标对象
     * @param ignoreProperties 忽略的属性名
     * @param <T> 目标类型
     * @return 目标对象
     */
    public static <T> T copyProperties(Object source, T target, String... ignoreProperties) {
        return copyProperties(source, target, false, ignoreProperties);
    }

    /**
     * 拷贝源对象属性到目标对象
     * @param source 源对象
     * @param target 目标对象
     * @param ignoreNull 是否忽略null值
     * @param ignoreProperties 忽略的属性名
     * @param <T> 目标类型
     * @return 目标对象
     */
    public static <T> T copyProperties(Object source, T target, boolean ignoreNull, String... ignoreProperties) {
        if (source == null || target == null) {
            return target;
        }

        Set<String> ignoreSet = ignoreProperties != null ?
                new HashSet<>(Arrays.asList(ignoreProperties)) : Collections.emptySet();

        Class<?> sourceClass = source.getClass();
        Class<?> targetClass = target.getClass();

        Map<String, Field> sourceFields = getFields(sourceClass);
        Map<String, Field> targetFields = getFields(targetClass);

        for (Map.Entry<String, Field> entry : targetFields.entrySet()) {
            String fieldName = entry.getKey();
            Field targetField = entry.getValue();

            // 检查是否在忽略列表中
            if (ignoreSet.contains(fieldName)) {
                continue;
            }

            Field sourceField = sourceFields.get(fieldName);
            if (sourceField != null && isAssignable(sourceField.getType(), targetField.getType())) {
                try {
                    Object value = getFieldValue(source, sourceField);
                    // 如果忽略null值且值为null，则跳过
                    if (ignoreNull && value == null) {
                        continue;
                    }
                    setFieldValue(target, targetField, value);
                } catch (Exception e) {
                    // 记录日志或处理异常
                    System.err.println("Copy property error: " + fieldName + ", " + e.getMessage());
                }
            }
        }

        return target;
    }

    /**
     * 创建目标类实例并拷贝属性
     * @param source 源对象
     * @param targetClass 目标类
     * @param <T> 目标类型
     * @return 目标对象实例
     */
    public static <T> T copyProperties(Object source, Class<T> targetClass) {
        if (source == null || targetClass == null) {
            return null;
        }

        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            return copyProperties(source, target);
        } catch (Exception e) {
            throw new RuntimeException("Create target instance failed: " + targetClass.getName(), e);
        }
    }

    /**
     * 拷贝列表中的对象
     * @param sourceList 源对象列表
     * @param targetClass 目标类
     * @param <S> 源类型
     * @param <T> 目标类型
     * @return 目标对象列表
     */
    public static <S, T> List<T> copyListProperties(List<S> sourceList, Class<T> targetClass) {
        if (sourceList == null || sourceList.isEmpty()) {
            return new ArrayList<>();
        }

        List<T> targetList = new ArrayList<>(sourceList.size());
        for (S source : sourceList) {
            T target = copyProperties(source, targetClass);
            targetList.add(target);
        }
        return targetList;
    }

    /**
     * 获取对象字段值
     */
    private static Object getFieldValue(Object obj, Field field) throws Exception {
        field.setAccessible(true);
        return field.get(obj);
    }

    /**
     * 设置对象字段值
     */
    private static void setFieldValue(Object obj, Field field, Object value) throws Exception {
        field.setAccessible(true);
        field.set(obj, value);
    }

    /**
     * 获取类的所有字段（包括父类）
     */
    private static Map<String, Field> getFields(Class<?> clazz) {
        return FIELD_CACHE.computeIfAbsent(clazz, k -> {
            Map<String, Field> fieldMap = new HashMap<>();
            Class<?> currentClass = clazz;
            while (currentClass != null && currentClass != Object.class) {
                Field[] fields = currentClass.getDeclaredFields();
                for (Field field : fields) {
                    // 忽略静态字段
                    if (!java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                        fieldMap.putIfAbsent(field.getName(), field);
                    }
                }
                currentClass = currentClass.getSuperclass();
            }
            return fieldMap;
        });
    }

    /**
     * 检查类型是否可赋值
     */
    private static boolean isAssignable(Class<?> sourceType, Class<?> targetType) {
        if (sourceType == targetType) {
            return true;
        }
        if (targetType.isAssignableFrom(sourceType)) {
            return true;
        }
        // 处理基本类型和包装类型的转换
        if (sourceType.isPrimitive() && targetType.isPrimitive()) {
            return true;
        }
        if (sourceType.isPrimitive()) {
            Class<?> wrapper = getWrapperClass(sourceType);
            return wrapper == targetType;
        }
        if (targetType.isPrimitive()) {
            Class<?> wrapper = getWrapperClass(targetType);
            return wrapper == sourceType;
        }
        return false;
    }

    /**
     * 获取基本类型的包装类
     */
    private static Class<?> getWrapperClass(Class<?> primitiveClass) {
        if (primitiveClass == int.class) {
            return Integer.class;
        }
        if (primitiveClass == long.class) {
            return Long.class;
        }
        if (primitiveClass == double.class) {
            return Double.class;
        }
        if (primitiveClass == float.class) {
            return Float.class;
        }
        if (primitiveClass == boolean.class) {
            return Boolean.class;
        }
        if (primitiveClass == byte.class) {
            return Byte.class;
        }
        if (primitiveClass == char.class) {
            return Character.class;
        }
        if (primitiveClass == short.class) {
            return Short.class;
        }
        return primitiveClass;
    }

    /**
     * 判断对象是否为空
     */
    public static boolean isEmpty(Object obj) {
        if (obj == null) {
            return true;
        }
        if (obj instanceof CharSequence) {
            return ((CharSequence) obj).length() == 0;
        }
        if (obj instanceof Collection) {
            return ((Collection<?>) obj).isEmpty();
        }
        if (obj instanceof Map) {
            return ((Map<?, ?>) obj).isEmpty();
        }
        if (obj instanceof Object[]) {
            return ((Object[]) obj).length == 0;
        }
        return false;
    }

    /**
     * 判断对象是否不为空
     */
    public static boolean isNotEmpty(Object obj) {
        return !isEmpty(obj);
    }
}
