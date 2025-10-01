package com.xxl.tool.http;

import com.xxl.tool.core.MapTool;
import com.xxl.tool.core.StringTool;
import com.xxl.tool.http.http.HttpRequest;
import com.xxl.tool.http.http.enums.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author xuxueli 2018-11-25 00:55:31
 */
public class HttpTool {
    private static final Logger logger = LoggerFactory.getLogger(HttpTool.class);

    // ---------------------- build ----------------------

    /**
     * build HttpRequest
     *
     * @return HttpRequest
     */
    public static HttpRequest createRequest() {
        return new HttpRequest();
    }

    /**
     * build HttpRequest
     *
     * @param url request url
     * @return HttpRequest
     */
    public static HttpRequest createRequest(String url) {
        return new HttpRequest().url(url);
    }

    /**
     * build GET HttpRequest
     *
     * @param url request url
     * @return HttpRequest
     */
    public static HttpRequest createGet(String url) {
        return new HttpRequest().url(url).method(Method.GET);
    }

    /**
     * build POST HttpRequest
     *
     * @param url request url
     * @return HttpRequest
     */
    public static HttpRequest createPost(String url) {
        return new HttpRequest().url(url).method(Method.POST);
    }

    // ---------------------- tool: http valid ----------------------

    /**
     * 检测是否https
     */
    public static boolean isHttps(String url) {
        return url != null && url.toLowerCase().startsWith("https:");
    }

    /**
     * 检测是否http
     */
    public static boolean isHttp(String url) {
        return url != null && url.toLowerCase().startsWith("http:");
    }

    // ---------------------- tool: url-params 2map ----------------------

    /**
     * 将Map转为 param 字符串
     *
     * <pre>
     *     {"k1", "v1", "k2", "v2"}     =  k1=v1&k2=v2
     * </pre>
     */
    public static String mapToUrlParams(Map<String, String> map) {
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
     * <pre>
     *     k1=v1&k2=v2     =  {"k1", "v1", "k2", "v2"}
     * </pre>
     */
    public static Map<String, String> urlParamsToMap(String urlParams) {
        Map<String, String> map = MapTool.newMap();
        if (StringTool.isNotBlank(urlParams)) {
            String[] params = urlParams.split("&");
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