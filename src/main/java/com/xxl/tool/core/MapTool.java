package com.xxl.tool.core;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * pipeline config loader, local version
 *
 * @author xuxueli 2024-01-01
 */
public class MapTool {

    /**
     * check if is empty.
     *
     * @param map  the map to check
     * @return
     */
    public static boolean isEmpty(final Map<?,?> map) {
        return map == null || map.isEmpty();
    }

    /**
     * check if is not empty.
     *
     * @param map  the map to check
     * @return
     */
    public static boolean isNotEmpty(final Map<?,?> map) {
        return !isEmpty(map);
    }

    // ---------------------- type safe getter ----------------------

    /**
     * get string value
     *
     * @param map
     * @param key
     * @return
     * @param <K>
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
     * get Boolean value
     *
     * @param map
     * @param key
     * @return
     * @param <K>
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
     * get Number value
     *
     * @param map
     * @param key
     * @return
     * @param <K>
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
     * get Byte value
     *
     * @param map
     * @param key
     * @return
     * @param <K>
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
     * get Short value
     *
     * @param map
     * @param key
     * @return
     * @param <K>
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
     * get Integer value
     *
     * @param map
     * @param key
     * @return
     * @param <K>
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
     * get Long value
     *
     * @param map
     * @param key
     * @return
     * @param <K>
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
     * get Float value
     *
     * @param map
     * @param key
     * @return
     * @param <K>
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
     * get Double value
     *
     * @param map
     * @param key
     * @return
     * @param <K>
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
     * build new HashMap
     *
     * @return
     * @param <K>
     * @param <V>
     */
    public static <K, V> HashMap<K, V> newHashMap() {
        return new HashMap<K, V>();
    }

    /**
     * build new HashMap （multi）
     *
     * @param k1
     * @param v1
     * @return
     * @param <K>
     * @param <V>
     */
    public static <K, V> HashMap<K, V> newHashMap(K k1, V v1) {
        HashMap<K, V> map = new HashMap<>();
        map.put(k1, v1);
        return map;
    }
    /**
     * build new HashMap （multi）
     */
    public static <K, V> HashMap<K, V> newHashMap(K k1, V v1, K k2, V v2) {
        HashMap<K, V> map = new HashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        return map;
    }
    /**
     * build new HashMap （multi）
     */
    public static <K, V> HashMap<K, V> newHashMap(K k1, V v1, K k2, V v2, K k3, V v3) {
        HashMap<K, V> map = new HashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        return map;
    }
    /**
     * build new HashMap （multi）
     */
    public static <K, V> HashMap<K, V> newHashMap(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        HashMap<K, V> map = new HashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        map.put(k4, v4);
        return map;
    }
    /**
     * build new HashMap （multi）
     */
    public static <K, V> HashMap<K, V> newHashMap(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
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
