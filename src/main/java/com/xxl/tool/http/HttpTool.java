package com.xxl.tool.http;

import com.xxl.tool.core.MapTool;
import com.xxl.tool.core.StringTool;
import com.xxl.tool.http.client.HttpClient;
import com.xxl.tool.http.http.HttpRequest;
import com.xxl.tool.http.http.enums.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * HTTP 工具：请求/客户端构建入口，以及 URL 与参数的互转
 *
 * <p>
 * {@link #createRequest()} 返回默认 POST 的请求构建器；{@link #createClient()} 返回基于接口代理的客户端构建器。
 * 本类全部为静态无状态方法，线程安全；构建出的 {@code HttpRequest}/{@code HttpClient} 为可变对象，不保证线程安全，
 * 每次请求应使用独立实例。
 * </p>
 *
 * @author xuxueli 2018-11-25 00:55:31
 */
public class HttpTool {
    private static final Logger logger = LoggerFactory.getLogger(HttpTool.class);

    // ---------------------- build basic ----------------------

    /**
     * build HttpRequest
     *
     * <p>
     * 返回未设置 URL 的请求构建器，默认方法为 POST、Content-Type 为 JSON。
     * </p>
     *
     * @return 新的 HttpRequest 实例，非 {@code null}
     */
    public static HttpRequest createRequest() {
        return new HttpRequest();
    }

    /**
     * build HttpRequest
     *
     * <p>
     * 设置 URL 并默认使用 POST 方法；需要 GET 请改用 {@link #createGet(String)}。
     * </p>
     *
     * @param url request url
     * @return 新的 HttpRequest 实例，非 {@code null}
     */
    public static HttpRequest createRequest(String url) {
        return new HttpRequest().url(url).method(Method.POST);
    }

    /**
     * build GET HttpRequest
     *
     * @param url request url
     * @return 方法为 GET 的 HttpRequest 实例，非 {@code null}
     */
    public static HttpRequest createGet(String url) {
        return new HttpRequest().url(url).method(Method.GET);
    }

    /**
     * build POST HttpRequest
     *
     * @param url request url
     * @return 方法为 POST 的 HttpRequest 实例，非 {@code null}
     */
    public static HttpRequest createPost(String url) {
        return new HttpRequest().url(url).method(Method.POST);
    }

    // ---------------------- build HttpClient ----------------------

    /**
     * build HttpClient
     *
     * <p>
     * 返回的客户端默认 POST、Content-Type 为 JSON、连接与读取超时均为 3000ms。
     * </p>
     *
     * @return 新的 HttpClient 实例，非 {@code null}
     */
    public static HttpClient createClient() {
        return new HttpClient();
    }


    // ---------------------- tool: http valid ----------------------

    /**
     * 检测是否https
     *
     * <p>
     * 忽略大小写判断是否以 {@code https:} 开头；不校验 URL 是否合法。
     * </p>
     *
     * @param url 待检测 URL，可为 {@code null}
     * @return 以 {@code https:} 开头返回 {@code true}；{@code null} 或其他协议返回 {@code false}
     */
    public static boolean isHttps(String url) {
        return url != null && url.toLowerCase().startsWith("https:");
    }

    /**
     * 检测是否http
     *
     * <p>
     * 忽略大小写判断是否以 {@code http:} 开头；不校验 URL 是否合法。
     * </p>
     *
     * @param url 待检测 URL，可为 {@code null}
     * @return 以 {@code http:} 开头返回 {@code true}；{@code null} 或其他协议返回 {@code false}
     */
    public static boolean isHttp(String url) {
        return url != null && url.toLowerCase().startsWith("http:");
    }

    // ---------------------- tool: url-params 2map ----------------------

    /**
     * 将Map转为 param 字符串
     *
     * <p>
     * key 与 value 均按 UTF-8 做 URL 编码，使用 {@code &} 连接。
     * </p>
     *
     * <pre>
     *     {"k1", "v1", "k2", "v2"}     =  k1=v1&k2=v2
     * </pre>
     *
     * @param map 参数 Map，可为 {@code null} 或空
     * @return 编码后的参数字符串；Map 为空时返回 {@code null}；value 为 {@code null} 时抛 {@link NullPointerException}
     */
    public static String generateUrlParam(Map<String, String> map) {
        if (MapTool.isEmpty(map)) {
            return null;
        }
        StringBuilder param = new StringBuilder();
        for (String key : map.keySet()) {
            if (!param.isEmpty()) {
                param.append("&");
            }
            param.append(URLEncoder.encode(key, StandardCharsets.UTF_8))
                    .append("=")
                    .append(URLEncoder.encode(map.get(key), StandardCharsets.UTF_8));
        }
        return param.toString();
    }

    /**
     * 将 param 字符串转为 Map
     *
     * <p>
     * 若入参包含 {@code ?}，只解析其后的查询串；按 {@code &} 与 {@code =} 简单切分，
     * <b>不做 URL 解码</b>，且只保留恰好两段的键值对（值中含 {@code =} 的项会被丢弃）。
     * 这是 {@link #generateUrlParam(Map)} 的逆操作，但不会还原百分号编码。
     * </p>
     *
     * <pre>
     *     k1=v1&k2=v2                              =  {"k1", "v1", "k2", "v2"}
     *     http://www.baidu.com?k1=v1&k2=v2         =  {"k1", "v1", "k2", "v2"}
     * </pre>
     *
     * @param url 查询串或完整 URL，可为 {@code null}
     * @return 解析结果，始终非 {@code null}；无有效参数时返回空 Map
     */
    public static Map<String, String> parseUrlParam(String url) {
        // parse url
        url = (url != null && url.contains("?"))
                ? url.substring(url.indexOf("?") + 1)
                : url;

        // parse param
        Map<String, String> map = MapTool.newMap();
        if (StringTool.isNotBlank(url)) {
            String[] params = url.split("&");
            for (String param : params) {
                String[] kv = param.split("=");
                if (kv.length == 2) {
                    map.put(kv[0], kv[1]);
                }
            }
        }
        return map;
    }


}