package com.xxl.tool.http.http;

import com.xxl.tool.core.AssertTool;
import com.xxl.tool.core.CollectionTool;
import com.xxl.tool.core.MapTool;
import com.xxl.tool.core.StringTool;
import com.xxl.tool.json.GsonTool;
import com.xxl.tool.http.HttpTool;
import com.xxl.tool.http.http.auth.AuthProvider;
import com.xxl.tool.http.http.auth.impl.ApiKeyAuthProvider;
import com.xxl.tool.http.http.auth.impl.BasicAuthProvider;
import com.xxl.tool.http.http.auth.impl.BearerTokenAuthProvider;
import com.xxl.tool.http.http.enums.ContentType;
import com.xxl.tool.http.http.enums.Header;
import com.xxl.tool.http.http.enums.Method;
import com.xxl.tool.http.http.iface.HttpInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xxl.tool.io.FileTool;
import com.xxl.tool.io.IOTool;
import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

import static com.xxl.tool.http.HttpTool.isHttps;

/**
 * Http Request
 */
public class HttpRequest {
    private static final Logger logger = LoggerFactory.getLogger(HttpRequest.class);

    // --------------------------------- field ---------------------------------

    private String url;                                     // 请求 Url
    private Method method = Method.POST;                    // Method
    private ContentType contentType = ContentType.JSON;     // Content-Type
    private Map<String, String> headers;                    // 存储请求头
    private Map<String, String> cookies;                    // Cookie（需要格式转换）
    private int connectTimeout = 3 * 1000;                  // 连接超时时间，单位：ms
    private int readTimeout = 3 * 1000;                     // 读取超时时间，单位：ms
    private boolean useCaches = false;                      // 是否使用缓存
    private String body;                                    // 存储请求体
    private Map<String, String> form;                       // 存储表单数据
    private AuthProvider authProvider;                      // 鉴权提供者
    private List<HttpInterceptor> interceptors;             // HttpInterceptor


    // --------------------------------- build ---------------------------------

    /**
     * 设置URL
     */
    public HttpRequest url(String url) {
        this.url = url;
        return this;
    }

    /**
     * 设置Method
     */
    public HttpRequest method(Method method) {
        this.method = method;
        return this;
    }

    /**
     * 设置Content-Type
     */
    public HttpRequest contentType(ContentType contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * 设置请求头，覆盖更新
     */
    public HttpRequest header(Map<String, String> header) {
        // valid
        if (MapTool.isEmpty(header)) {
            return this;
        }

        if (MapTool.isNotEmpty(headers)) {
            headers.clear();
        }
        for (String key : header.keySet()) {
            header(key, header.get(key));
        }
        return this;
    }

    /**
     * 设置请求头
     */
    public HttpRequest header(String key, String value) {
        // valid
        if (StringTool.isBlank(key) || Objects.isNull(value)) {
            return this;
        }

        // init
        if (null == this.headers) {
            this.headers = new HashMap<>();
        }

        // set
        this.headers.put(key, value);
        return this;
    }

    /**
     * 设置请求头, 默认值
     */
    public HttpRequest headerDefault() {
        header(Header.USER_AGENT.getValue(), Header.DEFAULT_USER_AGENT_WIN);
        return this;
    }

    /**
     * 设置Cookie，覆盖更新
     */
    public HttpRequest cookie(Map<String, String> cookie) {
        // valid
        if (MapTool.isEmpty(cookie)) {
            return this;
        }

        if (MapTool.isNotEmpty(this.cookies)) {
            this.cookies.clear();
        }
        for (String key : cookie.keySet()) {
            cookie(key, cookie.get(key));
        }
        return this;
    }

    /**
     * 设置Cookie
     */
    public HttpRequest cookie(String key, String value) {
        // valid
        if (StringTool.isBlank(key) || Objects.isNull(value)) {
            return this;
        }

        // init
        if (null == this.cookies) {
            this.cookies = new HashMap<>();
        }

        // set
        this.cookies.put(key, value);
        return this;
    }

    /**
     * 设置连接超时时间
     */
    public HttpRequest connectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    /**
     * 设置读取超时时间
     */
    public HttpRequest readTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    public HttpRequest useCaches(boolean useCaches) {
        this.useCaches = useCaches;
        return this;
    }

    /**
     * 设置请求体 body
     */
    public HttpRequest body(String body) {
        this.body = body;
        return this;
    }

    /**
     * 设置请求体 request
     *
     * @param request 请求体对象，将会序列化为 json (body 数据)
     * @return this
     */
    public HttpRequest request(Object request) {
        if (Objects.nonNull(request)) {
            this.body = GsonTool.toJson(request);
        }
        return this;
    }

    /**
     * 设置表单 form
     */
    public HttpRequest form(String key, String value) {
        // valid
        if (StringTool.isBlank(key) || Objects.isNull(value)) {
            return this;
        }

        // init
        if (null == this.form) {
            this.form = new HashMap<>();
        }

        // set form
        this.form.put(key, value);
        return this;
    }

    /**
     * 设置表单 form
     */
    public HttpRequest form(String key, List<String> value) {
        if (CollectionTool.isEmpty(value)) {
            return this;
        }
        return form(key, StringTool.join(value, ","));
    }

    /**
     * 设置表单 form，覆盖更新
     */
    public HttpRequest form(Map<String, String> form) {
        if (MapTool.isEmpty(form)) {
            return this;
        }

        if (MapTool.isNotEmpty(this.form)) {
            this.form.clear();
        }
        for (String key : form.keySet()) {
            form(key, form.get(key));
        }
        return this;
    }

    /**
     * 设置鉴权提供者
     */
    public HttpRequest auth(AuthProvider authProvider) {
        this.authProvider = authProvider;
        return this;
    }

    /**
     * 设置 Basic Auth
     */
    public HttpRequest basicAuth(String username, String password) {
        this.authProvider = new BasicAuthProvider(username, password);
        return this;
    }

    /**
     * 设置 Bearer Token Auth
     */
    public HttpRequest bearerAuth(String token) {
        this.authProvider = new BearerTokenAuthProvider(token);
        return this;
    }

    /**
     * 设置 API Key Auth
     */
    public HttpRequest apiKeyAuth(String keyName, String keyValue, ApiKeyAuthProvider.Location location) {
        this.authProvider = new ApiKeyAuthProvider(keyName, keyValue, location);
        return this;
    }

    /**
     * 默认设置 API Key Auth, 默认位置为 HEADER
     */
    public HttpRequest apiKeyAuth(String keyName, String keyValue) {
        this.authProvider = new ApiKeyAuthProvider(keyName, keyValue, ApiKeyAuthProvider.Location.HEADER);
        return this;
    }

    /**
     * 设置拦截器，覆盖更新
     */
    public HttpRequest interceptor(List<HttpInterceptor> interceptors) {
        if (CollectionTool.isEmpty(interceptors)) {
            return this;
        }

        if (CollectionTool.isNotEmpty(this.interceptors)) {
            this.interceptors.clear();
        }
        for (HttpInterceptor interceptor : interceptors) {
            interceptor(interceptor);
        }
        return this;
    }

    /**
     * 设置拦截器
     */
    public HttpRequest interceptor(HttpInterceptor interceptor) {
        if (Objects.isNull(interceptor)) {
            return this;
        }

        if (null == interceptors) {
            this.interceptors = new ArrayList<>();
        }
        interceptors.add(interceptor);
        return this;
    }


    // --------------------------------- get ---------------------------------

    /**
     * 获取格式化后的 Cookie: name1=value1; name2=value2; name3=value3
     * （Cookie 格式是一个键值对字符串，多个键值对之间用分号和空格分隔）
     */
    public String getCookieString() {
        if (MapTool.isEmpty(cookies)) {
            return null;
        }
        StringBuilder cookieHeader = new StringBuilder();
        for (String key : cookies.keySet()) {
            if (!cookieHeader.isEmpty() && !cookieHeader.toString().endsWith("; ")) {
                cookieHeader.append("; ");
            }
            cookieHeader.append(key).append("=").append(cookies.get(key));
        }

        return cookieHeader.toString();
    }

    public String getUrl() {
        return url;
    }

    public Method getMethod() {
        return method;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public Map<String, String> getHeader() {
        return headers;
    }

    public Map<String, String> getCookie() {
        return cookies;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public boolean isUseCaches() {
        return useCaches;
    }

    public String getBody() {
        return body;
    }

    public Map<String, String> getForm() {
        return form;
    }

    public AuthProvider getAuthProvider() {
        return authProvider;
    }


    // --------------------------------- invoke ---------------------------------

    /**
     * init HttpURLConnection
     *
     * @return HttpURLConnection
     */
    private HttpURLConnection prepareConnection() {
        // valid
        AssertTool.isTrue(StringTool.isNotBlank(this.url), "http-request url is null");
        AssertTool.isTrue(Objects.nonNull(this.method), "http-request method is null");
        AssertTool.isTrue(Objects.nonNull(this.contentType), "http-request contentType is null");

        // init connection
        HttpURLConnection connection = null;
        try {
            // parse url
            String finalUrl = this.url;
            if (Method.GET == this.method) {
                // write form-data to url (only GET method)
                String formParam = HttpTool.generateUrlParam(this.form);
                if (StringTool.isNotBlank(formParam)) {
                    finalUrl = finalUrl + (finalUrl.contains("?") ? "&" : "?") + formParam;
                }
            }

            // auth provider: may modify header and url
            if (this.authProvider != null) {
                this.authProvider.apply(this);
            }

            // open conection
            URL finalUrlURL = new URL(finalUrl);
            connection = (HttpURLConnection) finalUrlURL.openConnection();

            // default: trust-https
            if (isHttps(finalUrl)) {
                HttpsURLConnection https = (HttpsURLConnection) connection;
                trustAllHosts(https);
            }

            // default: setting
            connection.setDoOutput(true);       // allow write data to url connection (output: local -> remote)
            connection.setDoInput(true);        // allow read data from url connection (input: remote -> local)
            connection.setRequestProperty(Header.CONNECTION.getValue(), "Keep-Alive");
            connection.setRequestProperty(Header.ACCEPT_CHARSET.getValue(), StandardCharsets.UTF_8.toString());

            // custome: setting
            connection.setRequestMethod(this.method.toString());
            connection.setRequestProperty(Header.CONTENT_TYPE.getValue(), this.contentType.getValue(StandardCharsets.UTF_8));
            connection.setReadTimeout(this.readTimeout);
            connection.setConnectTimeout(this.connectTimeout);
            connection.setUseCaches(this.useCaches);

            // custome: header
            if (MapTool.isNotEmpty(this.headers)) {
                for (String key : this.headers.keySet()) {
                    connection.setRequestProperty(key, this.headers.get(key));
                }
            }

            // custome: cookie
            if (MapTool.isNotEmpty(this.cookies)) {
                String cookieString = getCookieString();
                connection.setRequestProperty(Header.COOKIE.getValue(), cookieString);
            }

            // inteceptor before
            if (CollectionTool.isNotEmpty(this.interceptors)) {
                for (HttpInterceptor interceptor : this.interceptors) {
                    interceptor.before(this);
                }
            }

            // do connection
            connection.connect();

            // write request-body
            if (Method.GET != this.method) {
                String requestBody = null;
                if (StringTool.isNotBlank(this.body)) {
                    requestBody = this.body;
                } else if (MapTool.isNotEmpty(this.form)) {
                    requestBody = HttpTool.generateUrlParam(this.form);
                }

                // write body-data (only POST method): output = local -> remote
                if (StringTool.isNotBlank(requestBody)) {
                    try (DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream())) {
                        dataOutputStream.write(requestBody.getBytes(StandardCharsets.UTF_8));
                        dataOutputStream.flush();
                    }
                }
            }

            return connection;
        } catch (Exception e) {
            // disconnect when error
            try {
                if (connection != null) {
                    connection.disconnect();
                }
            } catch (Exception e2) {
                logger.error(e2.getMessage(), e2);
            }
            throw new RuntimeException("Http Request Error (" + e.getMessage() + "). for url : " + url, e);
        }
    }

    /**
     * send http request
     *
     * @return HttpResponse
     */
    public HttpResponse execute() {
        HttpURLConnection connection = null;
        try {
            // prepare connection
            connection = prepareConnection();

            // init httpResponse
            HttpResponse httpResponse = new HttpResponse();
            httpResponse.setUrl(this.url);
            httpResponse.setStatusCode(connection.getResponseCode());

            // write response-body （ parse from inputStream, consider error )
            InputStream inputStream = httpResponse.isSuccess()
                    ? connection.getInputStream()
                    : connection.getErrorStream();
            if (inputStream != null) {
                try (BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        result.append(line);
                    }
                    httpResponse.setResponse(result.toString());
                }
            }

            // write set-cookie / request-cookie
            httpResponse.setCookies(parseResponseCookieData(connection));

            // inteceptor after
            if (CollectionTool.isNotEmpty(this.interceptors)) {
                for (HttpInterceptor interceptor : this.interceptors) {
                    interceptor.after(this, httpResponse);
                }
            }

            return httpResponse;
        } catch (Exception e) {
            throw new RuntimeException("Http Request Error (" + e.getMessage() + "). for url : " + url, e);
        } finally {
            // disconnect when finish
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * download file, save to destFile
     *
     * @param parentPath    parent path
     * @param destFile      dest file
     * @return
     */
    public long download(String parentPath, String destFile) {
        return download(FileTool.file(parentPath, destFile).getPath());
    }

    /**
     * download file, save to destFile
     *
     * @param destFile destFile
     * @return file size
     */
    public long download(String destFile) {
        HttpURLConnection connection = null;
        try {
            File destFileNew = FileTool.file(destFile);
            // valid destFilePath
            if (FileTool.isDirectory(destFileNew)) {
                throw new RuntimeException("Download fail, destFile is a directory for url : " + url);
            }
            if (FileTool.exists(destFileNew)) {
                // default overwrite file when exist
                FileTool.delete(destFileNew);
                //throw new RuntimeException("Download fail, file already exists for url : " + url);
            }
            FileTool.createParentDirectories(destFileNew);

            // prepare connection
            connection = prepareConnection();

            // validate statusCode
            int statusCode = connection.getResponseCode();
            if (statusCode != 200) {
                throw new RuntimeException("Download fail, statusCode(" + statusCode + ") for url : " + url);
            }

            // download to file: copy remote -> local
            try (InputStream in = connection.getInputStream();
                 OutputStream out = new FileOutputStream(destFileNew)) {
                return IOTool.copy(in, out, false, true);
            }
        } catch (IOException e) {
            // delete file when error
            if (FileTool.isFile(destFile)) {
                FileTool.delete(destFile);
            }
            throw new RuntimeException("Download error for url : " + url, e);
        } finally {
            // disconnect when finish
            if (connection != null) {
                connection.disconnect();
            }
        }
    }


    // --------------------------------- tool ---------------------------------

    /**
     * parse response-cookie from HttpURLConnection
     *
     * @param connection    connection of HttpURLConnection
     * @return cookieMap
     */
    private Map<String, String> parseResponseCookieData(HttpURLConnection connection) {
        Map<String, List<String>> headerFields = connection.getHeaderFields();
        List<String> setCookieHeaders = headerFields.get(Header.SET_COOKIE.getValue());
        if (CollectionTool.isEmpty(setCookieHeaders)) {
            return null;
        }

        Map<String, String> cookieMap = new HashMap<>();
        for (String setCookieHeader : setCookieHeaders) {
            String[] parts = setCookieHeader.split(";");
            if (parts.length > 0) {
                String cookiePart = parts[0];
                String[] cookieKeyValue = cookiePart.split("=", 2);
                if (cookieKeyValue.length == 2) {
                    cookieMap.put(cookieKeyValue[0], cookieKeyValue[1]);
                }
            }
        }
        return cookieMap;
    }

    /**
     * trust-https
     */
    private void trustAllHosts(HttpsURLConnection connection) {
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            SSLSocketFactory newFactory = sc.getSocketFactory();

            connection.setSSLSocketFactory(newFactory);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        connection.setHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
    }

    /**
     * trust-all-certs
     */
    private static final TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[]{};
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }
    }};

}