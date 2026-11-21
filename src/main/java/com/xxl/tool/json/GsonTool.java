package com.xxl.tool.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.ToNumberPolicy;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 基于 Gson 的 JSON 序列化/反序列化工具
 *
 * <p>可选依赖：{@code com.google.code.gson:gson}（作用域 {@code provided}），
 * 未引入该依赖时本类无法加载。</p>
 *
 * <p><b>配置：</b>日期统一格式化为 {@code yyyy-MM-dd HH:mm:ss}；关闭 HTML 转义
 * （{@code < > & = '} 不转义为 Unicode）；反序列化到 {@code Object} 时数字按
 * {@link ToNumberPolicy#LONG_OR_DOUBLE} 解析（整数保持整型，小数保持浮点）；
 * {@link #toJsonPretty(Object)} 额外启用缩进美化。</p>
 *
 * <p><b>null 语义：</b>{@code toJson(null)} 返回字符串 {@code "null"}；反序列化
 * {@code null}、空串或 {@code "null"} 返回 {@code null}；对象字段为 {@code null} 时默认不输出该字段。</p>
 *
 * <p><b>类型与格式：</b>反序列化按字段名精确匹配（不做下划线/驼峰转换），泛型需通过
 * {@link Type}、{@link #fromJsonList(String, Class)} 或 {@link #fromJsonMap(String, Class, Class)}
 * 保留类型信息；不支持 {@code java.time} 类型的默认格式化（需自行注册适配器）。</p>
 *
 * <p><b>线程安全：</b>内部共享的 {@link Gson} 实例是线程安全的，所有方法为静态方法，
 * 不修改入参对象。</p>
 *
 * @author xuxueli 2020-04-11 20:56:31
 */
public class GsonTool {

    private static Gson gson = null;
    private static Gson gsonPretty = null;
    static {
        gson= new GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .disableHtmlEscaping()
                .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
                .create();
        gsonPretty = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .disableHtmlEscaping()
                .setPrettyPrinting()
                .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
                .create();
    }

    /**
     * Object 转成 json
     *
     * <pre>
     *     String json = GsonTool.toJson(new Demo());
     * </pre>
     *
     * @param src       object，可为 {@code null}
     * @return String   json string；{@code src} 为 {@code null} 时返回字符串 {@code "null"}
     */
    public static String toJson(Object src) {
        return gson.toJson(src);
    }

    /**
     * Object 转成 json
     *
     * <pre>
     *     String json = GsonTool.toJsonPretty(new Demo());
     * </pre>
     *
     * @param src       object，可为 {@code null}
     * @return String   带缩进换行的 json string；{@code src} 为 {@code null} 时返回 {@code "null"}
     */
    public static String toJsonPretty(Object src) {
        return gsonPretty.toJson(src);
    }

    /**
     * json 转成 特定的cls的Object
     *
     * <pre>
     *     Demo demo = GsonTool.fromJson(json, Demo.class);
     * </pre>
     *
     * @param json      json 字符串，可为 {@code null}
     * @param classOfT  目标类型
     * @return 反序列化对象；{@code json} 为 {@code null}、空串或 {@code "null"} 时返回 {@code null}
     * @throws com.google.gson.JsonSyntaxException json 格式非法或与目标类型不匹配时抛出
     */
    public static <T> T fromJson(String json, Class<T> classOfT) {
        return gson.fromJson(json, classOfT);
    }

    /**
     * json 转成 特定的 Type 的Object
     *
     * @param json      json 字符串，可为 {@code null}
     * @param typeOfT   目标类型，通常来自 {@link TypeToken#getType()}
     * @return 反序列化对象；{@code json} 为 {@code null}、空串或 {@code "null"} 时返回 {@code null}
     * @param <T>       目标类型
     * @throws com.google.gson.JsonSyntaxException json 格式非法或与目标类型不匹配时抛出
     */
    public static <T> T  fromJson(String json, Type typeOfT) {
        return gson.fromJson(json, typeOfT);
    }

    /**
     * json 转成 特定的 Type 的Object
     *
     * <pre>
     *     Response<Demo> response = GsonTool.fromJson(json, Response.class, Demo.class);
     * </pre>
     *
     * @param json          json 字符串，可为 {@code null}
     * @param rawType       原始类型，如 {@code Response.class}
     * @param typeArguments 泛型参数类型，如 {@code Demo.class}
     * @return 反序列化对象；{@code json} 为 {@code null}、空串或 {@code "null"} 时返回 {@code null}
     * @param <T>           目标类型
     * @throws com.google.gson.JsonSyntaxException json 格式非法或与目标类型不匹配时抛出
     */
    public static <T> T  fromJson(String json, Type rawType, Type... typeArguments) {
        Type type = TypeToken.getParameterized(rawType, typeArguments).getType();
        return gson.fromJson(json, type);
    }

    /**
     * json 转成 特定的cls的 ArrayList
     *
     * <pre>
     *     List<Demo> demoList = GsonTool.fromJsonList(json, Demo.class);
     * </pre>
     *
     * @param json      json 数组字符串，可为 {@code null}
     * @param classOfT  列表元素类型
     * @return 反序列化后的 {@link ArrayList}；{@code json} 为 {@code null}、空串或 {@code "null"} 时返回 {@code null}
     * @param <T>       元素类型
     * @throws com.google.gson.JsonSyntaxException json 格式非法或与目标类型不匹配时抛出
     */
    public static <T> ArrayList<T> fromJsonList(String json, Class<T> classOfT) {
        Type type = TypeToken.getParameterized(ArrayList.class, classOfT).getType();
        return gson.fromJson(json, type);
    }

    /**
     * json 转成 特定的cls的 HashMap
     *
     * <pre>
     *     HashMap<String, Demo> map = GsonTool.fromJsonMap(json, String.class, Demo.class);
     * </pre>
     *
     * @param json       json 对象字符串，可为 {@code null}
     * @param keyClass   键类型
     * @param valueClass 值类型
     * @return 反序列化后的 {@link HashMap}；{@code json} 为 {@code null}、空串或 {@code "null"} 时返回 {@code null}
     * @param <K>        键类型
     * @param <V>        值类型
     * @throws com.google.gson.JsonSyntaxException json 格式非法或与目标类型不匹配时抛出
     */
    public static <K, V> HashMap<K, V> fromJsonMap(String json, Class<K> keyClass, Class<V> valueClass) {
        Type type = TypeToken.getParameterized(HashMap.class, keyClass, valueClass).getType();
        return gson.fromJson(json, type);
    }

    // ---------------------------------

    /**
     * Object 转成 JsonElement
     *
     * @param src 待转换对象，可为 {@code null}
     * @return JSON 树节点；{@code src} 为 {@code null} 时返回 {@link com.google.gson.JsonNull}
     */
    public static JsonElement toJsonElement(Object src) {
        return gson.toJsonTree(src);
    }

    /**
     * JsonElement 转成 特定的cls的Object
     *
     * @param json     JSON 树节点，可为 {@code null}
     * @param classOfT 目标类型
     * @return 反序列化对象；{@code json} 为 {@code null} 或 {@code JsonNull} 时返回 {@code null}
     * @param <T>      目标类型
     * @throws com.google.gson.JsonSyntaxException 节点与目标类型不匹配时抛出
     */
    public static <T> T fromJsonElement(JsonElement json, Class<T> classOfT) {
        return gson.fromJson(json, classOfT);
    }

    /**
     * JsonElement 转成 特定的 rawClass&lt;classOfT&gt; 的Object
     *
     * @param json     JSON 树节点，可为 {@code null}
     * @param typeOfT  目标类型，通常来自 {@link TypeToken#getType()}
     * @return 反序列化对象；{@code json} 为 {@code null} 或 {@code JsonNull} 时返回 {@code null}
     * @param <T>      目标类型
     * @throws com.google.gson.JsonSyntaxException 节点与目标类型不匹配时抛出
     */
    public static <T> T fromJsonElement(JsonElement json, Type typeOfT) {
        return gson.fromJson(json, typeOfT);
    }

    /**
     * JsonElement 转成 特定的 Type 的 Object
     *
     * @param json          JSON 树节点，可为 {@code null}
     * @param rawType       原始类型，如 {@code Response.class}
     * @param typeArguments 泛型参数类型，如 {@code Demo.class}
     * @return 反序列化对象；{@code json} 为 {@code null} 或 {@code JsonNull} 时返回 {@code null}
     * @param <T>           目标类型
     * @throws com.google.gson.JsonSyntaxException 节点与目标类型不匹配时抛出
     */
    public static <T> T fromJsonElement(JsonElement json, Type rawType, Type... typeArguments) {
        Type typeOfT = TypeToken.getParameterized(rawType, typeArguments).getType();
        return gson.fromJson(json, typeOfT);
    }

}
