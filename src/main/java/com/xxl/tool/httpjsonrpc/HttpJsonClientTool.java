package com.xxl.tool.httpjsonrpc;

import com.xxl.tool.gson.GsonTool;
import com.xxl.tool.net.HttpTool;

import java.util.Map;

/**
 * @author xuxueli 2025-02-26
 */
public class HttpJsonClientTool {

    // ---------------------- base ----------------------

    /**
     * http post object
     *
     * @param url
     * @param requestObject
     * @param responseClass
     * @param timeout               by second
     * @param headers
     * @return
     * @param <T>
     */
    private static <T> T doPostObject(String url,
                                      Object requestObject,
                                      Class<T> responseClass,
                                      int timeout,
                                      Map<String, String> headers) {

        // request
        String requestJson = null;
        if (requestObject!=null) {
            requestJson = GsonTool.toJson(requestObject);
        }

        // response
        String responseJson = HttpTool.postBody(url, requestJson, timeout, headers);
        T responseObj = null;
        if (responseClass!=null) {
            if (String.class.equals(responseClass)) {
                responseObj = (T) responseJson;
            } else {
                responseObj = GsonTool.fromJson(responseJson, responseClass);
            }
        }

        return responseObj;
    }


    // ---------------------- post ----------------------

    /**
     * http post object
     *
     * @param url
     * @param requestObject
     * @param responseClass
     * @param timeout               by second
     * @param headers
     * @return
     * @param <T>
     */
    public static <T> T postObject(String url,
                                   Object requestObject,
                                   Class<T> responseClass,
                                   int timeout,
                                   Map<String, String> headers) {
        return doPostObject(url, requestObject, responseClass, timeout, headers);
    }

    /**
     * http post object, whth timeout=3s and no headers
     *
     * @param url
     * @param requestObject
     * @param responseClass
     * @return
     * @param <T>
     */
    public static <T> T postObject(String url,
                                   Object requestObject,
                                   Class<T> responseClass) {

        return doPostObject(url, requestObject, responseClass, 3, null);
    }

}
