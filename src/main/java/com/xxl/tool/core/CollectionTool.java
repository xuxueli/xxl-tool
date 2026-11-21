package com.xxl.tool.core;

import java.util.*;

/**
 * 集合工具
 *
 * <p>覆盖集合的空值判断、包含判断、集合运算（并集 / 交集 / 对称差 / 差集）、
 * 新建 ArrayList 与按批拆分等能力。所有方法均为静态方法、无状态且线程安全；
 * 除 {@link #split(List, int)} 返回子列表视图外，集合运算方法均返回新集合且不修改入参。</p>
 *
 * @author xuxueli 2024-01-21 05:03:10
 * (some references to other libraries)
 */
public class CollectionTool {
    /** 复用的计数 1，避免计数过程中重复装箱。 */
    private static Integer INTEGER_ONE = Integer.valueOf(1);

    // ---------------------- empty  ----------------------

    /**
     * 判断集合是否为空（null 安全）。
     *
     * <pre>
     * CollectionTool.isEmpty(null)                    = true
     * CollectionTool.isEmpty(new ArrayList())         = true
     * CollectionTool.isEmpty(Arrays.asList("a"))      = false
     * </pre>
     *
     * @param coll 待校验集合，可为 {@code null}
     * @return 集合为 {@code null} 或不含任何元素时返回 {@code true}
     */
    public static boolean isEmpty(Collection<?> coll) {
        return (coll == null || coll.isEmpty());
    }

    /**
     * 判断集合是否不为空（null 安全）。
     *
     * <pre>
     * CollectionTool.isNotEmpty(null)                 = false
     * CollectionTool.isNotEmpty(new ArrayList())      = false
     * CollectionTool.isNotEmpty(Arrays.asList("a"))   = true
     * </pre>
     *
     * @param coll 待校验集合，可为 {@code null}
     * @return 集合非 {@code null} 且至少包含一个元素时返回 {@code true}
     */
    public static boolean isNotEmpty(Collection<?> coll) {
        return !isEmpty(coll);
    }

    // ---------------------- contains  ----------------------

    /**
     * 判断集合是否包含指定元素（null 安全）。
     *
     * <p>集合为 {@code null} 或空时直接返回 {@code false}；否则委托 {@link Collection#contains(Object)}，
     * 是否命中 {@code null} 元素取决于集合实现（如 {@link ArrayList} 允许包含 {@code null}）。</p>
     *
     * @param collection 待搜索集合，可为 {@code null}
     * @param value      待查找元素，可为 {@code null}
     * @return 集合非空且包含该元素时返回 {@code true}
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
     * 并集：按元素的最大出现次数合并两个集合。
     *
     * <p>返回新的 {@link ArrayList}，不修改入参；每个元素在结果中保留两个集合中较大的出现次数。
     * 结果顺序取决于 {@link HashSet} 的迭代顺序，不保证与入参顺序一致。
     * 任一入参为 {@code null} 时抛出 {@link NullPointerException}。</p>
     *
     * <p>示例：a = [1,2,3,3,4,5]、b = [3,4,4,5,6,7] 时，结果为 [1,2,3,3,4,4,5,6,7]（顺序不作保证）。</p>
     *
     * @param a 第一个集合，不能为 {@code null}
     * @param b 第二个集合，不能为 {@code null}
     * @return 包含并集元素的新集合
     * @see Collection#addAll
     */
    public static <E> Collection<E> union(final Collection<? extends E> a, final Collection<? extends E> b) {
        List<E> list = new ArrayList<>();
        Map<E, Integer> mapa = getCardinalityMap(a);
        Map<E, Integer> mapb = getCardinalityMap(b);
        Set<E> elts = new HashSet<>(a);
        elts.addAll(b);
        Iterator<E> it = elts.iterator();
        while(it.hasNext()) {
            E obj = it.next();
            for(int i=0,m=Math.max(getFreq(obj,mapa),getFreq(obj,mapb));i<m;i++) {
                list.add(obj);
            }
        }
        return list;
    }

    /**
     * 获取元素在计数映射中的出现次数，不存在时返回 0。
     */
    private static int getFreq(final Object obj, final Map<?, Integer> freqMap) {
        Integer count = freqMap.get(obj);
        if (count != null) {
            return count.intValue();
        }
        return 0;
    }

    /**
     * 统计元素出现次数。
     *
     * <p>返回新的 {@link HashMap}，key 为集合中的元素，value 为该元素在集合中的出现次数；
     * 仅集合中实际存在的元素会作为 key，不修改入参。入参为 {@code null} 时抛出
     * {@link NullPointerException}。</p>
     *
     * @param coll 待统计集合，不能为 {@code null}
     * @return 元素到出现次数的映射
     */
    public static <E> Map<E, Integer> getCardinalityMap(final Collection<? extends E> coll) {
        Map<E, Integer> count = new HashMap<>();
        for (E obj : coll) {
            Integer c = count.get(obj);
            if (c == null) {
                count.put(obj,INTEGER_ONE);
            } else {
                count.put(obj, Integer.valueOf(c.intValue() + 1));
            }
        }
        return count;
    }

    /**
     * 交集：按元素的最小出现次数保留两个集合共有的元素。
     *
     * <p>返回新的 {@link ArrayList}，不修改入参；每个元素在结果中保留两个集合中较小的出现次数。
     * 结果顺序取决于 {@link HashSet} 的迭代顺序，不保证与入参顺序一致。
     * 任一入参为 {@code null} 时抛出 {@link NullPointerException}。</p>
     *
     * <p>示例：a = [1,2,3,3,4,5]、b = [3,4,4,5,6,7] 时，结果为 [3,4,5]（顺序不作保证）。</p>
     *
     * @param a 第一个集合，不能为 {@code null}
     * @param b 第二个集合，不能为 {@code null}
     * @return 包含交集元素的新集合
     */
    public static <E> Collection<E> intersection(final Collection<? extends E> a, final Collection<? extends E> b) {
        List<E> list = new ArrayList<>();
        Map<E, Integer> mapa = getCardinalityMap(a);
        Map<E, Integer> mapb = getCardinalityMap(b);
        Set<E> elts = new HashSet<>(a);
        elts.addAll(b);
        Iterator<E> it = elts.iterator();
        while(it.hasNext()) {
            E obj = it.next();
            for(int i=0,m=Math.min(getFreq(obj,mapa),getFreq(obj,mapb));i<m;i++) {
                list.add(obj);
            }
        }
        return list;
    }

    /**
     * 交集的补集（对称差）：保留仅出现在其中一个集合中的元素。
     *
     * <p>返回新的 {@link ArrayList}，不修改入参；每个元素在结果中保留两个集合出现次数之差的绝对值，
     * 即两边出现次数相同的元素不会保留。结果顺序取决于 {@link HashSet} 的迭代顺序，
     * 不保证与入参顺序一致。任一入参为 {@code null} 时抛出 {@link NullPointerException}。</p>
     *
     * <p>示例：a = [1,2,3,3,4,5]、b = [3,4,4,5,6,7] 时，结果为 [1,2,3,4,6,7]（顺序不作保证）。</p>
     *
     * @param a 第一个集合，不能为 {@code null}
     * @param b 第二个集合，不能为 {@code null}
     * @return 包含对称差元素的新集合
     */
    public static <E> Collection<E> disjunction(final Collection<? extends E> a, final Collection<? extends E> b) {
        List<E> list = new ArrayList<>();
        Map<E, Integer> mapa = getCardinalityMap(a);
        Map<E, Integer> mapb = getCardinalityMap(b);
        Set<E> elts = new HashSet<>(a);
        elts.addAll(b);
        Iterator<E> it = elts.iterator();
        while(it.hasNext()) {
            E obj = it.next();
            for(int i=0,m=((Math.max(getFreq(obj,mapa),getFreq(obj,mapb)))-(Math.min(getFreq(obj,mapa),getFreq(obj,mapb))));i<m;i++) {
                list.add(obj);
            }
        }
        return list;
    }

    /**
     * A 与 B 的差（A - B）：从 A 的副本中逐个移除 B 中的元素。
     *
     * <p>返回新的 {@link ArrayList}，不修改入参；B 中的每个元素最多从 A 副本中移除一次，
     * 因此重复元素按出现次数扣减。任一入参为 {@code null} 时抛出 {@link NullPointerException}。</p>
     *
     * <p>示例：a = [1,2,3,3,4,5]、b = [3,4,4,5,6,7] 时，结果为 [1,2,3]；
     * 交换入参后 subtract(b, a) 的结果为 [4,6,7]。</p>
     *
     * @param a 被减集合，不能为 {@code null}
     * @param b 要移除的集合，不能为 {@code null}
     * @return 包含差集结果的新集合，保持 A 中剩余元素的相对顺序
     * @see Collection#removeAll
     */
    public static <E> Collection<E> subtract(final Collection<? extends E> a, final Collection<? extends E> b) {
        List<E> list = new ArrayList<>( a );
        for (Object obj : b) {
            list.remove(obj);
        }
        return list;
    }

    // ---------------------- new  ----------------------

    /**
     * 创建空的 ArrayList。
     *
     * @return 新建的可变空 {@link ArrayList}
     * @param <E> 元素类型
     */
    public static <E> ArrayList<E> newArrayList() {
        return new ArrayList<E>();
    }

    /**
     * 创建包含指定元素的 ArrayList。
     *
     * <p>返回新的可变 {@link ArrayList}，元素顺序与入参一致；{@code elements} 为空数组时返回空集合，
     * 为 {@code null} 时抛出 {@link NullPointerException}。</p>
     *
     * @param elements 初始元素，可变参数，可为空数组但不能为 {@code null}
     * @return 包含入参元素的新 ArrayList
     * @param <E> 元素类型
     */
    @SafeVarargs
    public static <E> ArrayList<E> newArrayList(E... elements) {
        ArrayList<E> list = new ArrayList<E>();
        Collections.addAll(list, elements);
        return list;
    }

    // ---------------------- split  ----------------------

    /**
     * 按固定批次大小拆分列表。
     *
     * <p>返回的每个批次都是原列表 {@link List#subList(int, int)} 的视图而非新列表：对原列表的修改会
     * 反映到已返回的批次中，拆分后对原列表的结构性修改可能导致视图抛出
     * {@link java.util.ConcurrentModificationException}。最后一批的元素数量可能小于 {@code batchSize}。</p>
     *
     * <p>{@code list} 为 {@code null}、空列表或 {@code batchSize <= 0} 时，
     * 返回 {@link Collections#emptyList()}（不可变空列表）。</p>
     *
     * <pre>
     * CollectionTool.split(Arrays.asList("1","2","3","4","5"), 2)
     *     = [["1","2"], ["3","4"], ["5"]]
     * </pre>
     *
     * @param list      待拆分列表，可为 {@code null}
     * @param batchSize 每个批次的元素数量，须为正数
     * @return 由子列表视图组成的批次列表；参数非法时返回不可变空列表
     * @param <E>       元素类型
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
