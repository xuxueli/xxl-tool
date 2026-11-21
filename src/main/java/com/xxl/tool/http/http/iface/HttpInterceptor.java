package com.xxl.tool.http.http.iface;

import com.xxl.tool.http.http.HttpRequest;
import com.xxl.tool.http.http.HttpResponse;

/**
 * HTTP 拦截器：在请求执行前后扩展处理逻辑
 *
 * <p>
 * 执行顺序：多个拦截器按注册顺序依次调用 {@link #before(HttpRequest)}，
 * 请求完成后仍按同一顺序调用 {@link #after(HttpRequest, HttpResponse)}（非栈式回绕）。
 * {@code before} 在连接已建立、请求头/ Cookie 已写入连接之后执行，因此在该阶段新增或修改请求头不会生效，
 * 但可直接修改请求对象的 URL、body 等字段（影响范围取决于底层已完成的连接配置）。
 * </p>
 *
 * <p>
 * 拦截器在请求线程中同步执行，应为无状态或自行保证线程安全；抛出的异常会中断请求并由
 * {@code execute} 包装为 {@link RuntimeException}。实现中不得记录认证头、Cookie 等敏感信息。
 * </p>
 */
public interface HttpInterceptor {

    /**
     * before execute
     *
     * <p>
     * 请求执行前调用；可读取请求配置。注意此时请求头已写入底层连接，修改 header 不会影响本次请求。
     * </p>
     *
     * @param httpRequest 当前请求对象，非 {@code null}
     */
    void before(HttpRequest httpRequest);

    /**
     * after execute
     *
     * <p>
     * 收到响应后、返回调用方前调用；可读取或替换响应内容。抛出的异常会中断返回。
     * </p>
     *
     * @param httpRequest  当前请求对象，非 {@code null}
     * @param httpResponse 当前响应对象，非 {@code null}
     */
    void after(HttpRequest httpRequest, HttpResponse httpResponse);

}