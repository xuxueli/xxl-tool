package com.xxl.tool.core;

import java.util.*;

/**
 * Collection Tool
 *
 * @author xuxueli 2024-01-21 05:03:10
 * (some references to other libraries)
 */
public class CollectionTool {
    private static Integer INTEGER_ONE = Integer.valueOf(1);

    // ---------------------- empty  ----------------------

    /**
     * Null-safe check if the specified collection is empty.
     * <p>
     * Null returns true.
     *
     * @param coll  the collection to check, may be null
     * @return true if empty or null
     * @since Commons Collections 3.2
     */
    public static boolean isEmpty(Collection coll) {
        return (coll == null || coll.isEmpty());
    }

    /**
     * Null-safe check if the specified collection is not empty.
     * <p>
     * Null returns false.
     *
     * @param coll  the collection to check, may be null
     * @return true if non-null and non-empty
     * @since Commons Collections 3.2
     */
    public static boolean isNotEmpty(Collection coll) {
        return !isEmpty(coll);
    }

    // ---------------------- contains  ----------------------

    /**
     * Returns <code>true</code> if the value in collections.
     *
     * @param collection
     * @param value
     * @return
     */
    public static boolean contains(Collection<?> collection, Object value) {
        return isNotEmpty(collection) && collection.contains(value);
    }

    // ---------------------- operate  ----------------------
    /**
     *  a: {1,2,3,3,4,5}
     *  b: {3,4,4,5,6,7}
     *
     *  CollectionTool.union(a,b)(并集):              // {1,2,3,3,4,4,5,6,7}
     *  CollectionTool.intersection(a,b)(交集):       // {3,4,5}
     *  CollectionTool.disjunction(a,b)(交集的补集):   // {1,2,3,4,6,7}
     *  CollectionTool.subtract(a,b)(A与B的差):       // {1,2,3}
     *  CollectionTool.subtract(b,a)(B与A的差):       // {4,6,7}
     */

    /**
     * 并集
     * Returns a {@link Collection} containing the union of the given {@link Collection}s.
     *
     * @param a  the first collection, must not be null
     * @param b  the second collection, must not be null
     * @return  the union of the two collections
     * @see Collection#addAll
     */
    public static Collection union(final Collection a, final Collection b) {
        ArrayList list = new ArrayList();
        Map mapa = getCardinalityMap(a);
        Map mapb = getCardinalityMap(b);
        Set elts = new HashSet(a);
        elts.addAll(b);
        Iterator it = elts.iterator();
        while(it.hasNext()) {
            Object obj = it.next();
            for(int i=0,m=Math.max(getFreq(obj,mapa),getFreq(obj,mapb));i<m;i++) {
                list.add(obj);
            }
        }
        return list;
    }

    private static final int getFreq(final Object obj, final Map freqMap) {
        Integer count = (Integer) freqMap.get(obj);
        if (count != null) {
            return count.intValue();
        }
        return 0;
    }

    /**
     * 元素出现次数
     * Returns a {@link Map} mapping each unique element in the given
     * {@link Collection} to an {@link Integer} representing the number
     * of occurrences of that element in the {@link Collection}.
     * <p>
     * Only those elements present in the collection will appear as
     * keys in the map.
     *
     * @param coll  the collection to get the cardinality map for, must not be null
     * @return the populated cardinality map
     */
    public static Map getCardinalityMap(final Collection coll) {
        Map count = new HashMap();
        for (Iterator it = coll.iterator(); it.hasNext();) {
            Object obj = it.next();
            Integer c = (Integer) (count.get(obj));
            if (c == null) {
                count.put(obj,INTEGER_ONE);
            } else {
                count.put(obj, Integer.valueOf(c.intValue() + 1));
            }
        }
        return count;
    }

    /**
     * 交集
     * Returns a {@link Collection} containing the intersection of the given {@link Collection}s.
     *
     * @param a  the first collection, must not be null
     * @param b  the second collection, must not be null
     * @return the intersection of the two collections
     */
    public static Collection intersection(final Collection a, final Collection b) {
        ArrayList list = new ArrayList();
        Map mapa = getCardinalityMap(a);
        Map mapb = getCardinalityMap(b);
        Set elts = new HashSet(a);
        elts.addAll(b);
        Iterator it = elts.iterator();
        while(it.hasNext()) {
            Object obj = it.next();
            for(int i=0,m=Math.min(getFreq(obj,mapa),getFreq(obj,mapb));i<m;i++) {
                list.add(obj);
            }
        }
        return list;
    }

    /**
     * 交集的补集
     * Returns a {@link Collection} containing the exclusive disjunction (symmetric difference) of the given {@link Collection}s.
     *
     * @param a  the first collection, must not be null
     * @param b  the second collection, must not be null
     * @return the symmetric difference of the two collections
     */
    public static Collection disjunction(final Collection a, final Collection b) {
        ArrayList list = new ArrayList();
        Map mapa = getCardinalityMap(a);
        Map mapb = getCardinalityMap(b);
        Set elts = new HashSet(a);
        elts.addAll(b);
        Iterator it = elts.iterator();
        while(it.hasNext()) {
            Object obj = it.next();
            for(int i=0,m=((Math.max(getFreq(obj,mapa),getFreq(obj,mapb)))-(Math.min(getFreq(obj,mapa),getFreq(obj,mapb))));i<m;i++) {
                list.add(obj);
            }
        }
        return list;
    }

    /**
     * A与B的差 (A-B)
     * Returns a new {@link Collection} containing <tt><i>a</i> - <i>b</i></tt>.
     *
     * @param a  the collection to subtract from, must not be null
     * @param b  the collection to subtract, must not be null
     * @return a new collection with the results
     * @see Collection#removeAll
     */
    public static Collection subtract(final Collection a, final Collection b) {
        ArrayList list = new ArrayList( a );
        for (Iterator it = b.iterator(); it.hasNext();) {
            list.remove(it.next());
        }
        return list;
    }

    // ---------------------- new  ----------------------

    /**
     * bulid new ArrayList
     *
     * @return
     * @param <E>
     */
    public static <E> ArrayList<E> newArrayList() {
        return new ArrayList<E>();
    }

    /**
     * bulid new ArrayList (with data)
     *
     * @param elements
     * @return
     * @param <E>
     */
    public static <E> ArrayList<E> newArrayList(E... elements) {
        ArrayList<E> list = new ArrayList<E>();
        Collections.addAll(list, elements);
        return list;
    }

    // ---------------------- split  ----------------------

    /**
     * split to batch-list
     *
     * @param list
     * @param batchSize
     * @return
     * @param <E>
     */
    public static <E> List<List<E>> split(List<E> list, int batchSize) {
        if (list == null || list.isEmpty() || batchSize <= 0) {
            return Collections.emptyList();
        }

        List<List<E>> result = new ArrayList<>();
        int totalSize = list.size();
        for (int i = 0; i < totalSize; i += batchSize) {
            int end = Math.min(i + batchSize, totalSize);
            result.add(list.subList(i, end));
        }
        return result;
    }

    // ---------------------- other  ----------------------

}
