package com.xxl.tool.core;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bean Tool / Bean 工具：提供 JavaBean 与 Map 的相互转换、属性拷贝及对象判空能力。
 *
 * <p>覆盖范围：
 * <ul>
 *   <li>Bean 字段值的递归转换，将嵌套 Bean、集合、Map 展平为 Map 结构（{@link #convertBeanFieldToMap(Object)}）；</li>
 *   <li>Bean 与 Map 双向转换，支持按字段名过滤（{@link #beanToMap(Object, String...)}、{@link #mapToBean(Map, Class, String...)}）；</li>
 *   <li>对象之间的属性浅拷贝，支持忽略指定字段与忽略 null 值（{@link #copyProperties(Object, Object, boolean, String...)}）；</li>
 *   <li>基于类型、字符串、集合、Map、对象数组的判空（{@link #isEmpty(Object)}）。</li>
 * </ul>
 *
 * <p>所有方法均为静态方法；字段读写通过反射完成，会调用 {@link Field#setAccessible(boolean)} 打开非 public 字段。
 * 字段元信息保存在进程级静态缓存（{@link ConcurrentHashMap}）中，可并发访问，缓存条目超过 500 个类时整体清空。
 *
 * @author xuxueli 2025-11-30
 */
public class BeanTool {

    // ---------------------- bean-field vs map-field ----------------------

    /**
     * 将 Bean 的字段值递归转换为 Map 结构：Collection 转为新的 {@link ArrayList}，Map 转为新的 {@link HashMap}，
     * 普通 Bean 转为 {@link #beanToMap(Object, String...)} 的结果。
     *
     * <p>仅处理非基本类型、非包装类型、非 {@link String} 的对象，其余值原样返回，因此转换结果中不再保留嵌套的原始 Bean 引用。
     * 顶层入参为 {@code null} 或基本类型/包装类型/String 时直接返回原值。
     *
     * @param value 待转换的值，可为 {@code null}
     * @return 转换后的值；入参为 {@code null} 时返回 {@code null}
     */
    public static Object convertBeanFieldToMap(Object value) {
        // parse complex object, such as Collection, Map, Bean;
        if (value!=null && !ClassTool.isPrimitiveOrWrapperOrString(value.getClass())) {
            if (value instanceof Collection<?> collection) {
                // convert collection
                ArrayList<Object> result = new ArrayList<>();
                for (Object item : collection) {
                    item = convertBeanFieldToMap(item);
                    result.add(item);
                }

                value = result;
            } else if (value instanceof Map<?, ?> map) {
                // convert map
                Map<Object, Object> result = new HashMap<>();
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    Object convertedKey = convertBeanFieldToMap(entry.getKey());
                    Object convertedValue = convertBeanFieldToMap(entry.getValue());

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
     * 将单个值转换为目标类型，通常由 {@link #mapToBean(Map, Class, String...)} 调用。
     *
     * <p>转换规则按优先级：
     * <ol>
     *   <li>入参为 {@code null} 时返回 {@code null}；</li>
     *   <li>{@code targetClass} 与入参类型相同或为其父类型时，原样返回；</li>
     *   <li>目标为基本类型/包装类型时，优先按 {@link Number} 转换，否则按字符串解析；</li>
     *   <li>目标为 {@code char}/{@code Character} 时取字符串首字符，空字符串返回 {@code '\0'}；</li>
     *   <li>目标为枚举时按名称转换（{@link Enum#valueOf(Class, String)}）；</li>
     *   <li>入参为 {@link Map} 时递归调用 {@link #mapToBean(Map, Class, String...)}；</li>
     *   <li>其余情况原样返回，不做强制类型转换。</li>
     * </ol>
     *
     * <p>字符串无法解析为对应数值时抛出 {@link NumberFormatException}，枚举名称非法时抛出 {@link IllegalArgumentException}。
     *
     * @param value       待转换的值，可为 {@code null}
     * @param targetClass 目标类型，不可为 {@code null}
     * @return 转换后的值；入参为 {@code null} 时返回 {@code null}
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
            // Enum.valueOf 的泛型签名无法直接接受 Class<?>，类型已由 isEnum() 校验
            @SuppressWarnings({"unchecked", "rawtypes"})
            Object enumValue = Enum.valueOf((Class<? extends Enum>) targetClass, String.valueOf(value));
            return enumValue;
        }

        // 2、convert map
        if (value instanceof Map) {
            // 仅按字段名取值并逐字段转换，非 String 键不会命中字段，运行时安全
            @SuppressWarnings("unchecked")
            Map<String, Object> valueMap = (Map<String, Object>) value;
            return mapToBean(valueMap, targetClass);
        }

        // 3、pass
        return value;
    }

    // ---------------------- bean vs map ----------------------

    /**
     * 将 Bean 转换为 Map：key 为字段名，value 为字段值；嵌套的 Bean、集合、Map 会经
     * {@link #convertBeanFieldToMap(Object)} 递归转换为 Map。
     *
     * <p>仅处理入参类自身声明的实例字段（使用 {@link Class#getDeclaredFields()}），不包含父类字段；static 字段始终跳过。
     * 读取私有字段前会调用 {@link Field#setAccessible(boolean)}，会修改字段的 accessible 标记。
     * {@code properties} 中未匹配任何字段的名称会被忽略，不抛异常。
     *
     * @param bean       待转换的 Bean，可为 {@code null}
     * @param properties 需要转换的字段名；为 {@code null} 或空数组时转换全部字段
     * @return 字段名到字段值的 Map；{@code bean} 为 {@code null} 时返回 {@code null}
     * @throws RuntimeException 字段读取失败时抛出（包装 {@link IllegalAccessException}）
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
     * 将 Map 转换为 Bean：按字段名匹配，把 Map 中的值经 {@link #convertMapFieldToBean(Object, Class)} 转换后写入同名字段。
     *
     * <p>目标对象通过无参构造器创建（构造器必须可访问）；仅处理目标类自身声明的实例字段（使用 {@link Class#getDeclaredFields()}），
     * 不包含父类字段，static 与 final 字段始终跳过。Map 中不存在对应字段的 key 会被忽略，缺失的字段保持默认值。
     * 写入字段前会调用 {@link Field#setAccessible(boolean)}。
     *
     * @param map         字段名到字段值的映射，可为 {@code null}
     * @param targetClass 目标 Bean 类型，可为 {@code null}
     * @param properties  需要转换的字段名；为 {@code null} 或空数组时转换全部字段
     * @param <T>         目标 Bean 类型
     * @return 转换后的 Bean；{@code map} 或 {@code targetClass} 为 {@code null} 时返回 {@code null}
     * @throws RuntimeException 实例创建失败或字段写入失败时抛出
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
     * 拷贝属性（浅拷贝），不忽略源对象中的 null 值。
     *
     * <p>等价于 {@code copyProperties(source, target, false, ignoreProperties)}。
     *
     * @param source           源对象，可为 {@code null}
     * @param target           目标对象，可为 {@code null}
     * @param ignoreProperties 忽略的目标字段名，可为 {@code null} 或空数组
     * @param <T>              目标对象类型
     * @return 目标对象；{@code source} 或 {@code target} 为 {@code null} 时原样返回 {@code target}
     * @see #copyProperties(Object, Object, boolean, String...)
     */
    public static <T> T copyProperties(Object source, T target, String... ignoreProperties) {
        return copyProperties(source, target, false, ignoreProperties);
    }

    /**
     * 拷贝属性（浅拷贝）：按字段名匹配，把源对象字段值写入目标对象的同名字段。
     *
     * <p>规则：
     * <ul>
     *   <li>只遍历目标对象字段（含父类字段，子类同名字段优先），static 字段不参与；</li>
     *   <li>源对象必须存在同名字段，且类型满足 {@link ClassTool#isAssignable(Class, Class)}（目标字段类型 ← 源字段类型）；</li>
     *   <li>浅拷贝：集合、Map、Bean 等引用类型直接赋值，不复制其内容；</li>
     *   <li>{@code ignoreNull} 为 {@code true} 时跳过源值为 {@code null} 的字段，否则用 {@code null} 覆盖目标值；</li>
     *   <li>读写字段前会调用 {@link Field#setAccessible(boolean)}，会修改字段的 accessible 标记。</li>
     * </ul>
     *
     * <p>字段元信息取自进程级静态缓存（{@link ConcurrentHashMap}，最多缓存 500 个类），方法可安全并发调用。
     *
     * @param source           源对象，可为 {@code null}
     * @param target           目标对象，可为 {@code null}
     * @param ignoreNull       是否忽略源对象中的 {@code null} 值
     * @param ignoreProperties 忽略的目标字段名，可为 {@code null} 或空数组
     * @param <T>              目标对象类型
     * @return 目标对象；{@code source} 或 {@code target} 为 {@code null} 时原样返回 {@code target}
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
     * 创建目标类的实例（无参构造器）并拷贝同名属性。
     *
     * <p>等价于先通过 {@link Class#getDeclaredConstructor(Class[])} 实例化目标类，再调用
     * {@link #copyProperties(Object, Object, String...)}。
     *
     * @param source      源对象，可为 {@code null}
     * @param targetClass 目标类型，可为 {@code null}
     * @param <T>         目标对象类型
     * @return 新建并完成拷贝的目标对象；{@code source} 或 {@code targetClass} 为 {@code null} 时返回 {@code null}
     * @throws RuntimeException 目标类无法实例化（无可用无参构造器等）时抛出
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
     * 批量拷贝：按源列表顺序逐个实例化目标类并拷贝同名属性。
     *
     * <p>源列表中的 {@code null} 元素会得到 {@code null} 目标对象，并保留在结果列表中；结果列表长度与源列表一致。
     *
     * @param sourceList  源列表，可为 {@code null}
     * @param targetClass 目标类型
     * @param <S>         源类型
     * @param <T>         目标类型
     * @return 目标列表；{@code sourceList} 为 {@code null} 或空时返回新的空 {@link ArrayList}（非 {@code null}）
     * @throws RuntimeException 任一元素的目标对象无法实例化时抛出
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
     * 判断对象是否为空。
     *
     * <p>判定规则：{@code null}、长度为 0 的 {@link CharSequence}、空 {@link Collection}、空 {@link Map}、
     * 长度为 0 的 {@code Object[]} 视为空；其余对象（包括 {@code int[]} 等基本类型数组）返回 {@code false}。
     *
     * <pre>
     * BeanTool.isEmpty(null)             = true
     * BeanTool.isEmpty("")               = true
     * BeanTool.isEmpty(new Object[0])    = true
     * BeanTool.isEmpty(new int[0])       = false
     * </pre>
     *
     * @param obj 待判断的对象，可为 {@code null}
     * @return 为空返回 {@code true}
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
     * 判断对象是否非空，语义与 {@link #isEmpty(Object)} 相反。
     *
     * @param obj 待判断的对象，可为 {@code null}
     * @return 非空返回 {@code true}；{@code null} 返回 {@code false}
     */
    public static boolean isNotEmpty(Object obj) {
        return !isEmpty(obj);
    }


    /**
     * 类到字段映射的缓存（key 为类，value 为「字段名 → Field」），缓存内容已合并父类字段。
     *
     * <p>使用 {@link ConcurrentHashMap} 保证并发读写安全；条目超过 500 个类时整体清空，避免长期运行导致内存膨胀。
     * 缓存中共享同一批 {@link Field} 实例，反射读写时会打开其 accessible 标记，该可变状态对所有调用方可见。
     */
    private static final Map<Class<?>, Map<String, Field>> FIELD_CACHE = new ConcurrentHashMap<>();

    /**
     * 获取类及其父类（不含 {@link Object}）的非静态字段，并写入进程级缓存。
     *
     * <p>遍历顺序为子类 → 父类，同名字段以子类为准（{@link Map#putIfAbsent}）。
     *
     * @param clazz 目标类，不可为 {@code null}
     * @return 字段名到 {@link Field} 的可变 Map；返回的是缓存内部实例，调用方不应修改
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
