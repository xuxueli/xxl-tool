package com.xxl.tool.core;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * Map 工具
 *
 * <p>覆盖 Map 的空值判断、按 key 安全读取并转换常用类型（String / Boolean / Number 及其子类型），
 * 以及快速构建 HashMap。所有方法均为静态方法、无状态且线程安全，不会修改入参 Map。</p>
 *
 * @author xuxueli 2024-01-01
 */
public class MapTool {

    // ---------------------- empty check ----------------------

    /**
     * 判断 Map 是否为空（null 安全）。
     *
     * <pre>
     * MapTool.isEmpty(null)              = true
     * MapTool.isEmpty(new HashMap&lt;&gt;())   = true
     * MapTool.isEmpty(Collections.singletonMap("k", "v")) = false
     * </pre>
     *
     * @param map 待校验 Map，可为 {@code null}
     * @return Map 为 {@code null} 或不含键值对时返回 {@code true}
     */
    public static boolean isEmpty(final Map<?,?> map) {
        return map == null || map.isEmpty();
    }

    /**
     * 判断 Map 是否不为空（null 安全）。
     *
     * <pre>
     * MapTool.isNotEmpty(null)              = false
     * MapTool.isNotEmpty(new HashMap&lt;&gt;())   = false
     * MapTool.isNotEmpty(Collections.singletonMap("k", "v")) = true
     * </pre>
     *
     * @param map 待校验 Map，可为 {@code null}
     * @return Map 非 {@code null} 且至少包含一个键值对时返回 {@code true}
     */
    public static boolean isNotEmpty(final Map<?,?> map) {
        return !isEmpty(map);
    }

    // ---------------------- type safe getter ----------------------

    /**
     * 按 key 读取并转为字符串。
     *
     * <p>Map 为 {@code null}、key 不存在或对应 value 为 {@code null} 时返回 {@code null}；
     * 否则返回 {@code value.toString()}（非 String 类型也会被转换）。</p>
     *
     * @param map 待读取 Map，可为 {@code null}
     * @param key 键，可为 {@code null}（取决于 Map 实现是否允许 null key）
     * @return 字符串值；未命中时返回 {@code null}
     * @param <K> 键类型
     */
    public static <K> String getString(final Map<? super K, ?> map, final K key) {
        if (map != null) {
            final Object value = map.get(key);
            if (value != null) {
                return value.toString();
            }
        }
        return null;
    }

    /**
     * 按 key 读取并转为 Boolean。
     *
     * <p>转换规则：{@link Boolean} 直接返回；{@link String} 使用 {@link Boolean#valueOf(String)}
     * （忽略大小写，仅 "true" 为 {@code true}，其余均为 {@code false}）；{@link Number} 非 0 为
     * {@code true}。其他类型、Map 为 {@code null}、key 不存在或 value 为 {@code null} 时返回
     * {@code null}。</p>
     *
     * @param map 待读取 Map，可为 {@code null}
     * @param key 键，可为 {@code null}（取决于 Map 实现是否允许 null key）
     * @return Boolean 值；未命中或无法转换时返回 {@code null}
     * @param <K> 键类型
     */
    public static <K> Boolean getBoolean(final Map<? super K, ?> map, final K key) {
        if (map != null) {
            final Object value = map.get(key);
            if (value != null) {
                if (value instanceof Boolean) {
                    return (Boolean) value;
                }
                if (value instanceof String) {
                    return Boolean.valueOf((String) value);
                }
                if (value instanceof Number) {
                    final Number n = (Number) value;
                    return n.intValue() != 0 ? Boolean.TRUE : Boolean.FALSE;
                }
            }
        }
        return null;
    }

    /**
     * 按 key 读取并转为 Number。
     *
     * <p>{@link Number} 直接返回；{@link String} 使用当前默认语言环境的
     * {@link NumberFormat#getInstance()} 解析（通常返回 {@link Long} 或 {@link Double}，
     * 解析结果受区域格式与千分位影响），解析失败返回 {@code null}。其他类型、Map 为 {@code null}、
     * key 不存在或 value 为 {@code null} 时返回 {@code null}。</p>
     *
     * @param map 待读取 Map，可为 {@code null}
     * @param key 键，可为 {@code null}（取决于 Map 实现是否允许 null key）
     * @return Number 值；未命中或解析失败时返回 {@code null}
     * @param <K> 键类型
     */
    public static <K> Number getNumber(final Map<? super K, ?> map, final K key) {
        if (map != null) {
            final Object value = map.get(key);
            if (value != null) {
                if (value instanceof Number) {
                    return (Number) value;
                }
                if (value instanceof String) {
                    try {
                        final String text = (String) value;
                        return NumberFormat.getInstance().parse(text);
                    } catch (final ParseException e) { // NOPMD
                        // failure means null is returned
                    }
                }
            }
        }
        return null;
    }

    /**
     * 按 key 读取并转为 Byte。
     *
     * <p>先经 {@link #getNumber(Map, Object)} 取数（支持 Number 与可解析的 String），
     * 再通过 {@link Number#byteValue()} 转换，超出 byte 范围时按窄化规则截断；
     * 未命中或无法解析时返回 {@code null}。</p>
     *
     * @param map 待读取 Map，可为 {@code null}
     * @param key 键，可为 {@code null}（取决于 Map 实现是否允许 null key）
     * @return Byte 值；未命中或无法解析时返回 {@code null}
     * @param <K> 键类型
     */
    public static <K> Byte getByte(final Map<? super K, ?> map, final K key) {
        final Number value = getNumber(map, key);
        if (value == null) {
            return null;
        }
        if (value instanceof Byte) {
            return (Byte) value;
        }
        return Byte.valueOf(value.byteValue());
    }

    /**
     * 按 key 读取并转为 Short。
     *
     * <p>先经 {@link #getNumber(Map, Object)} 取数（支持 Number 与可解析的 String），
     * 再通过 {@link Number#shortValue()} 转换，超出 short 范围时按窄化规则截断；
     * 未命中或无法解析时返回 {@code null}。</p>
     *
     * @param map 待读取 Map，可为 {@code null}
     * @param key 键，可为 {@code null}（取决于 Map 实现是否允许 null key）
     * @return Short 值；未命中或无法解析时返回 {@code null}
     * @param <K> 键类型
     */
    public static <K> Short getShort(final Map<? super K, ?> map, final K key) {
        final Number value = getNumber(map, key);
        if (value == null) {
            return null;
        }
        if (value instanceof Short) {
            return (Short) value;
        }
        return Short.valueOf(value.shortValue());
    }

    /**
     * 按 key 读取并转为 Integer。
     *
     * <p>先经 {@link #getNumber(Map, Object)} 取数（支持 Number 与可解析的 String），
     * 再通过 {@link Number#intValue()} 转换，超出 int 范围时按窄化规则截断；
     * 未命中或无法解析时返回 {@code null}。</p>
     *
     * @param map 待读取 Map，可为 {@code null}
     * @param key 键，可为 {@code null}（取决于 Map 实现是否允许 null key）
     * @return Integer 值；未命中或无法解析时返回 {@code null}
     * @param <K> 键类型
     */
    public static <K> Integer getInteger(final Map<? super K, ?> map, final K key) {
        final Number value = getNumber(map, key);
        if (value == null) {
            return null;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        return Integer.valueOf(value.intValue());
    }

    /**
     * 按 key 读取并转为 Long。
     *
     * <p>先经 {@link #getNumber(Map, Object)} 取数（支持 Number 与可解析的 String），
     * 再通过 {@link Number#longValue()} 转换；未命中或无法解析时返回 {@code null}。</p>
     *
     * @param map 待读取 Map，可为 {@code null}
     * @param key 键，可为 {@code null}（取决于 Map 实现是否允许 null key）
     * @return Long 值；未命中或无法解析时返回 {@code null}
     * @param <K> 键类型
     */
    public static <K> Long getLong(final Map<? super K, ?> map, final K key) {
        final Number value = getNumber(map, key);
        if (value == null) {
            return null;
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        return Long.valueOf(value.longValue());
    }

    /**
     * 按 key 读取并转为 Float。
     *
     * <p>先经 {@link #getNumber(Map, Object)} 取数（支持 Number 与可解析的 String），
     * 再通过 {@link Number#floatValue()} 转换；未命中或无法解析时返回 {@code null}。</p>
     *
     * @param map 待读取 Map，可为 {@code null}
     * @param key 键，可为 {@code null}（取决于 Map 实现是否允许 null key）
     * @return Float 值；未命中或无法解析时返回 {@code null}
     * @param <K> 键类型
     */
    public static <K> Float getFloat(final Map<? super K, ?> map, final K key) {
        final Number value = getNumber(map, key);
        if (value == null) {
            return null;
        }
        if (value instanceof Float) {
            return (Float) value;
        }
        return Float.valueOf(value.floatValue());
    }

    /**
     * 按 key 读取并转为 Double。
     *
     * <p>先经 {@link #getNumber(Map, Object)} 取数（支持 Number 与可解析的 String），
     * 再通过 {@link Number#doubleValue()} 转换；未命中或无法解析时返回 {@code null}。</p>
     *
     * @param map 待读取 Map，可为 {@code null}
     * @param key 键，可为 {@code null}（取决于 Map 实现是否允许 null key）
     * @return Double 值；未命中或无法解析时返回 {@code null}
     * @param <K> 键类型
     */
    public static <K> Double getDouble(final Map<? super K, ?> map, final K key) {
        final Number value = getNumber(map, key);
        if (value == null) {
            return null;
        }
        if (value instanceof Double) {
            return (Double) value;
        }
        return Double.valueOf(value.doubleValue());
    }


    // ---------------------- new ----------------------

    /**
     * 创建空的 HashMap。
     *
     * @return 新建的可变空 {@link HashMap}
     * @param <K> 键类型
     * @param <V> 值类型
     */
    public static <K, V> HashMap<K, V> newMap() {
        return new HashMap<K, V>();
    }

    /**
     * 创建包含一个键值对的 HashMap。
     *
     * <p>返回新的可变 {@link HashMap}，允许 {@code null} 键与 {@code null} 值。</p>
     *
     * @param k1 键
     * @param v1 值
     * @return 包含指定键值对的 HashMap
     * @param <K> 键类型
     * @param <V> 值类型
     */
    public static <K, V> HashMap<K, V> newMap(K k1, V v1) {
        HashMap<K, V> map = new HashMap<>();
        map.put(k1, v1);
        return map;
    }
    /**
     * 创建包含两个键值对的 HashMap。
     *
     * <p>返回新的可变 {@link HashMap}，允许 {@code null} 键与 {@code null} 值；
     * 若两个键相等（按 {@link Object#equals(Object)}），后写入的值会覆盖先前的值，最终只保留一个键值对。</p>
     *
     * @param k1 第一个键
     * @param v1 第一个值
     * @param k2 第二个键
     * @param v2 第二个值
     * @return 包含指定键值对的 HashMap
     * @param <K> 键类型
     * @param <V> 值类型
     */
    public static <K, V> HashMap<K, V> newMap(K k1, V v1, K k2, V v2) {
        HashMap<K, V> map = new HashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        return map;
    }
    /**
     * 创建包含三个键值对的 HashMap。
     *
     * <p>返回新的可变 {@link HashMap}，允许 {@code null} 键与 {@code null} 值；
     * 若键重复（按 {@link Object#equals(Object)}），后写入的值会覆盖先前的值。</p>
     *
     * @param k1 第一个键
     * @param v1 第一个值
     * @param k2 第二个键
     * @param v2 第二个值
     * @param k3 第三个键
     * @param v3 第三个值
     * @return 包含指定键值对的 HashMap
     * @param <K> 键类型
     * @param <V> 值类型
     */
    public static <K, V> HashMap<K, V> newMap(K k1, V v1, K k2, V v2, K k3, V v3) {
        HashMap<K, V> map = new HashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        return map;
    }
    /**
     * 创建包含四个键值对的 HashMap。
     *
     * <p>返回新的可变 {@link HashMap}，允许 {@code null} 键与 {@code null} 值；
     * 若键重复（按 {@link Object#equals(Object)}），后写入的值会覆盖先前的值。</p>
     *
     * @param k1 第一个键
     * @param v1 第一个值
     * @param k2 第二个键
     * @param v2 第二个值
     * @param k3 第三个键
     * @param v3 第三个值
     * @param k4 第四个键
     * @param v4 第四个值
     * @return 包含指定键值对的 HashMap
     * @param <K> 键类型
     * @param <V> 值类型
     */
    public static <K, V> HashMap<K, V> newMap(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        HashMap<K, V> map = new HashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        map.put(k4, v4);
        return map;
    }
    /**
     * 创建包含五个键值对的 HashMap。
     *
     * <p>返回新的可变 {@link HashMap}，允许 {@code null} 键与 {@code null} 值；
     * 若键重复（按 {@link Object#equals(Object)}），后写入的值会覆盖先前的值。</p>
     *
     * @param k1 第一个键
     * @param v1 第一个值
     * @param k2 第二个键
     * @param v2 第二个值
     * @param k3 第三个键
     * @param v3 第三个值
     * @param k4 第四个键
     * @param v4 第四个值
     * @param k5 第五个键
     * @param v5 第五个值
     * @return 包含指定键值对的 HashMap
     * @param <K> 键类型
     * @param <V> 值类型
     */
    public static <K, V> HashMap<K, V> newMap(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
        HashMap<K, V> map = new HashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        map.put(k4, v4);
        map.put(k5, v5);
        return map;
    }


    // ---------------------- other ----------------------


}
