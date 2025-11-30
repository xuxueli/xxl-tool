package com.xxl.tool.core;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * bean tool
 *
 * @author xuxueli 2025-11-30
 */
public class BeanTool {

    // ---------------------- convert object vs primitive ----------------------

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

    // ---------------------- convert bean vs map ----------------------

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
        Field[] fields = ReflectionTool.getAllFields(bean.getClass(), false);

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
            Field[] fields = ReflectionTool.getAllFields(targetClass, false);

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


}
