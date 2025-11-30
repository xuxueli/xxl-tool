package com.xxl.tool.core;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * bean tool
 *
 * @author xuxueli 2025-11-30
 */
public class BeanTool {

    // ---------------------- bean-field vs map-field ----------------------

    /**
     * convert bean-field to map
     *
     * @param value   the value to convert
     * @return convert result, bean-field will be converted to map
     */
    public static Object convertBeanFieldToMap(Object value) {
        // parse complex object, such as Collection, Map, Bean;
        if (value!=null && !ClassTool.isPrimitiveOrWrapperOrString(value.getClass())) {
            if (value instanceof Collection collection) {
                // convert collection
                ArrayList<Object> result = new ArrayList<>();
                for (Object item : collection) {
                    item = convertBeanFieldToMap(item);
                    result.add(item);
                }

                value = result;
            } else if (value instanceof Map map) {
                // convert map
                Map<Object, Object> result = new HashMap<>();
                for (Object mapKey : map.entrySet()) {
                    Object convertedKey = convertBeanFieldToMap(mapKey);
                    Object convertedValue = convertBeanFieldToMap(map.get(mapKey));

                    result.put(convertedKey, convertedValue);
                }

                value = result;
            } else {
                // convert bean
                value = beanToMap(value);
            }
        }
        return value;
    }

    /**
     * convert map-field to target javabean
     *
     * @param value   the value to convert
     * @param targetClass  target class
     * @return convert result, map-field will be converted to target javabean
     */
    public static Object convertMapFieldToBean(Object value, Class<?> targetClass) {
        if (value == null) {
            return null;
        }

        // skip if same type
        if (targetClass.isAssignableFrom(value.getClass())) {
            return value;
        }

        // 1、convert primitive type
        if (targetClass == boolean.class || targetClass == Boolean.class) {
            // boolean
            if (value instanceof Boolean) {
                return (Boolean) value;
            } else {
                return Boolean.valueOf(String.valueOf(value));
            }
        } else if (targetClass == byte.class || targetClass == Byte.class) {
            // byte
            if (value instanceof Number) {
                return ((Number) value).byteValue();
            } else {
                return Byte.valueOf(String.valueOf(value));
            }
        } else if (targetClass == short.class || targetClass == Short.class) {
            // short
            if (value instanceof Number) {
                return ((Number) value).shortValue();
            } else {
                return Short.valueOf(String.valueOf(value));
            }
        } else if (targetClass == int.class || targetClass == Integer.class) {
            // int
            if (value instanceof Number) {
                return ((Number) value).intValue();
            } else {
                return Integer.valueOf(String.valueOf(value));
            }
        } else if (targetClass == long.class || targetClass == Long.class) {
            // long
            if (value instanceof Number) {
                return ((Number) value).longValue();
            } else {
                return Long.valueOf(String.valueOf(value));
            }
        } else if (targetClass == float.class || targetClass == Float.class) {
            // float
            if (value instanceof Number) {
                return ((Number) value).floatValue();
            } else {
                return Float.valueOf(String.valueOf(value));
            }
        } else if (targetClass == double.class || targetClass == Double.class) {
            // double
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            } else {
                return Double.valueOf(String.valueOf(value));
            }
        } else if (targetClass == char.class || targetClass == Character.class) {
            // char
            if (value instanceof Character) {
                return (Character) value;
            } else {
                String str = String.valueOf(value);
                return str.isEmpty() ? '\0' : str.charAt(0);
            }
        } else if (targetClass == String.class) {
            // string
            return value.toString();
        }

        // enum
        if (targetClass.isEnum()) {
            return Enum.valueOf((Class<Enum>) targetClass, String.valueOf(value));
        }

        // 2、convert map
        if (value instanceof Map) {
            return mapToBean((Map<String, Object>) value, targetClass);
        }

        // 3、pass
        return value;
    }

    // ---------------------- bean vs map ----------------------

    /**
     * convert Bean to Map
     *
     * @param bean          bean to convert
     * @param properties    properties to convert, null means all properties
     * @return map contains all bean properties
     */
    public static Map<String, Object> beanToMap(Object bean, String... properties) {
        // valid
        if (bean == null) {
            return null;
        }
        Map<String, Object> resultMap = new HashMap<>();

        // get all fields
        Field[] fields = ReflectionTool.getFields(bean.getClass(), false);

        // property specified to convert
        Set<String> propertySet = new HashSet<>();
        if (properties != null && properties.length > 0) {
            propertySet.addAll(Arrays.asList(properties));
        }

        // convert field 2 map-entity
        for (Field field : fields) {
            // skip static fields
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            // skip properties if not specified
            if (!propertySet.isEmpty() && !propertySet.contains(field.getName())) {
                continue;
            }

            // field 2 map
            try {
                field.setAccessible(true);
                Object value = field.get(bean);

                // convert 2 primitive or map
                value = convertBeanFieldToMap(value);

                // write field value
                resultMap.put(field.getName(), value);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("beanToMap error, failed to get field value: " + field.getName(), e);
            }
        }

        return resultMap;
    }

    /**
     * convert Map to Bean
     *
     * @param map       map to convert
     * @param targetClass     target bean class
     * @param properties    properties to convert, null means all properties
     * @return target bean
     */
    public static <T> T mapToBean(Map<String, Object> map, Class<T> targetClass, String... properties) {
        if (map == null || targetClass == null) {
            return null;
        }

        try {
            // new instance
            T instance = targetClass.getDeclaredConstructor().newInstance();
            // get all fields
            Field[] fields = ReflectionTool.getFields(targetClass, false);

            // property specified to convert
            Set<String> propertySet = new HashSet<>();
            if (properties != null && properties.length > 0) {
                propertySet.addAll(Arrays.asList(properties));
            }

            // convert map-entity 2 field
            for (Field field : fields) {
                // skip static and final fields
                if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) {
                    continue;
                }

                // skip properties if not specified
                if (!propertySet.isEmpty() && !propertySet.contains(field.getName())) {
                    continue;
                }

                // map 2 field
                String fieldName = field.getName();
                if (map.containsKey(fieldName)) {
                    try {
                        field.setAccessible(true);
                        Object value = map.get(fieldName);

                        // convert 2 target class
                        Object convertedValue = convertMapFieldToBean(value, field.getType());

                        // write field value
                        field.set(instance, convertedValue);
                    } catch (Exception e) {
                        throw new RuntimeException("mapToBean error, failed to set field: " + fieldName, e);
                    }
                }
            }

            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance of " + targetClass.getSimpleName(), e);
        }
    }


    // ---------------------- copy properties ----------------------

    /**
     * copy properties
     *
     * @param source source object
     * @param target target object
     * @param ignoreProperties ignore properties
     * @return target object
     * @param <T> target class
     */
    public static <T> T copyProperties(Object source, T target, String... ignoreProperties) {
        return copyProperties(source, target, false, ignoreProperties);
    }

    /**
     * copy properties （浅拷贝）
     *
     * @param source source object
     * @param target target object
     * @param ignoreNull ignore null value
     * @param ignoreProperties ignore properties
     * @return target object
     * @param <T> target class
     */
    public static <T> T copyProperties(Object source, T target, boolean ignoreNull, String... ignoreProperties) {
        if (source == null || target == null) {
            return target;
        }

        // ignore properties
        Set<String> ignoreSet = ignoreProperties != null ?
                new HashSet<>(Arrays.asList(ignoreProperties)) : Collections.emptySet();

        // get class and fields
        Class<?> sourceClass = source.getClass();
        Class<?> targetClass = target.getClass();

        Map<String, Field> sourceFields = getAllFieldsExcludeStaticWithCache(sourceClass);
        Map<String, Field> targetFields = getAllFieldsExcludeStaticWithCache(targetClass);

        for (Map.Entry<String, Field> entry : targetFields.entrySet()) {
            // process target field
            String fieldName = entry.getKey();
            Field targetField = entry.getValue();

            // skip ignore properties
            if (ignoreSet.contains(fieldName)) {
                continue;
            }

            // get source field value
            Field sourceField = sourceFields.get(fieldName);
            if (sourceField != null && ClassTool.isAssignable(targetField.getType(), sourceField.getType())) {
                Object value = ReflectionTool.getFieldValue(sourceField, source);
                // skip null
                if (ignoreNull && value == null) {
                    continue;
                }
                ReflectionTool.setFieldValue(targetField, target, value);
            }
        }

        return target;
    }

    /**
     * copy properties
     *
     * @param source source object
     * @param targetClass target class
     * @return target object
     * @param <T> target class
     */
    public static <T> T copyProperties(Object source, Class<T> targetClass) {
        if (source == null || targetClass == null) {
            return null;
        }
        T target;
        try {
            target = targetClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Create target instance failed: " + targetClass.getName(), e);
        }
        return copyProperties(source, target);
    }

    /**
     * copy list properties
     *
     * @param sourceList    source list
     * @param targetClass   target class
     * @return target list
     * @param <S> source type
     * @param <T> target type
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


    // ---------------------- empty ----------------------

    /**
     * judge object is empty
     *
     * @param obj  object
     * @return true if empty
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
     * judge object is not empty
     * @param obj  object
     * @return true if not empty
     */
    public static boolean isNotEmpty(Object obj) {
        return !isEmpty(obj);
    }


    /**
     * field cache
     */
    private static final Map<Class<?>, Map<String, Field>> FIELD_CACHE = new ConcurrentHashMap<>();

    /**
     * get all field with cache, exclude static field
     */
    private static Map<String, Field> getAllFieldsExcludeStaticWithCache(final Class<?> clazz) {
        // avloid memory leaks
        if (FIELD_CACHE.size() > 500) {
            FIELD_CACHE.clear();
        }
        return FIELD_CACHE.computeIfAbsent(clazz, k -> {
            Map<String, Field> fieldMap = new HashMap<>();
            Class<?> currentClass = clazz;
            while (currentClass != null && currentClass != Object.class) {
                // get all fields (public/private/protected) or current class
                Field[] fields = currentClass.getDeclaredFields();
                for (Field field : fields) {
                    // skip static field
                    if (Modifier.isStatic(field.getModifiers())) {
                        continue;
                    }
                    fieldMap.putIfAbsent(field.getName(), field);
                }
                // process super class
                currentClass = currentClass.getSuperclass();
            }
            return fieldMap;
        });
    }


}
