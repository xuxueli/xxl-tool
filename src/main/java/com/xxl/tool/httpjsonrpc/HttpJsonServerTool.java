package com.xxl.tool.httpjsonrpc;

import com.xxl.tool.gson.GsonTool;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * @author xuxueli 2025-02-26
 */
public class HttpJsonServerTool {

    // ---------------------- function context ----------------------

    /**
     * default function context
     */
    private static final ConcurrentMap<String, Function> defaultFunctionContext = HttpJsonServerTool.newFunctionContext();

    /**
     * generate new function context
     *
     * @return
     */
    public static ConcurrentMap<String, Function> newFunctionContext(){
        return new ConcurrentHashMap<String, Function>();
    }

    /**
     * add function
     *
     * @param functionContext
     * @param url
     * @param function
     */
    public static void addFunction(ConcurrentMap<String, Function> functionContext, String url, Function function){
        functionContext.put(url, function);
    }

    /**
     * add function
     *
     * @param url
     * @param function
     */
    public static void addFunction(String url, Function function){
        addFunction(defaultFunctionContext, url, function);
    }


    // ---------------------- function ----------------------

    /**
     * invoke function
     *
     * @param functionContext
     * @param url
     * @param requestJson
     * @return
     */
    public static String invoke(ConcurrentMap<String, Function> functionContext,
                                String url,
                                String requestJson){

        // load function
        Function function = functionContext.get(url);
        if (function == null) {
            throw new RuntimeException("function not found with url["+url+"].");
        }

        // get input class
        Class<?> inputType = null;
        Type type = function.getClass().getGenericInterfaces()[0];
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            inputType = (Class<?>) actualTypeArguments[0];
            //Class<?> rType = (Class<?>) actualTypeArguments[1];
        }
        if (inputType == null) {
            throw new RuntimeException("function inputType invalid with url["+url+"].");
        }

        // json > request
        Object requestObj = GsonTool.fromJson(requestJson, inputType);

        // invoke function
        Object responseObj = function.apply(requestObj);

        // response > json
        String responseJson = GsonTool.toJson(responseObj);
        return responseJson;
    }

    /**
     * invoke function
     *
     * @param url
     * @param requestJson
     * @return
     */
    public static String invoke(String url,
                                String requestJson){

        return invoke(defaultFunctionContext, url, requestJson);
    }

}
