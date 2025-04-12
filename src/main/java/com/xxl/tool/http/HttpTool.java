package com.xxl.tool.http;

import com.xxl.tool.core.MapTool;
import com.xxl.tool.core.StringTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

/**
 * @author xuxueli 2018-11-25 00:55:31
 */
public class HttpTool {
    private static Logger logger = LoggerFactory.getLogger(HttpTool.class);


    // trust-https start
    private static void trustAllHosts(HttpsURLConnection connection) {
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
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
    private static final TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
        @Override
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[]{};
        }
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }
        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }
    }};
    // trust-https end

    /**
     * post
     *
     * @param method                "GET", "POST", "HEAD", "OPTIONS", "PUT", "DELETE", "TRACE"
     * @param url
     * @param requestBody
     * @param headers
     * @param timeout               by milliseconds
     * @return
     */
    private static String doRequest(String method,
                                    String url,
                                    String requestBody,
                                    Map<String, String> headers,
                                    int timeout) {

        HttpURLConnection connection = null;
        BufferedReader bufferedReader = null;
        try {
            // connection
            URL realUrl = new URL(url);
            connection = (HttpURLConnection) realUrl.openConnection();

            // trust-https
            boolean useHttps = url.startsWith("https");
            if (useHttps) {
                HttpsURLConnection https = (HttpsURLConnection) connection;
                trustAllHosts(https);
            }

            // connection setting
            connection.setRequestMethod(method);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setReadTimeout(timeout);
            connection.setConnectTimeout(3 * 1000);
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            connection.setRequestProperty("Accept-Charset", "application/json;charset=UTF-8");

            // header setting
            if(MapTool.isNotEmpty(headers)){
                for (String key : headers.keySet()) {
                    connection.setRequestProperty(key, headers.get(key));
                }
            }

            // do connection
            connection.connect();

            // write requestBody
            if (StringTool.isNotBlank(requestBody)) {
                DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
                dataOutputStream.write(requestBody.getBytes(StandardCharsets.UTF_8));
                dataOutputStream.flush();
                dataOutputStream.close();
            }

            // valid StatusCode
            int statusCode = connection.getResponseCode();
            if (statusCode != 200) {
                throw new RuntimeException("StatusCode("+ statusCode +") invalid. for url : " + url);
            }

            // result
            bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                result.append(line);
            }
            String responseBody = result.toString();

            // parse returnT
            return responseBody;
        } catch (Exception e) {
            throw new RuntimeException("Http Request Error ("+ e.getMessage() +"). for url : " + url);
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (connection != null) {
                    connection.disconnect();
                }
            } catch (Exception e2) {
                logger.error(e2.getMessage(), e2);
            }
        }
    }

    // ---------------------- post ----------------------

    /**
     * post
     *
     * @param url
     * @param requestBody
     * @param headers
     * @param timeout       by milliseconds
     * @return
     */
    public static String postBody(String url,
                                  String requestBody,
                                  Map<String, String> headers,
                                  int timeout
                                  ) {
        return doRequest("POST", url, requestBody, headers, timeout);
    }

    /**
     * post
     *
     * @param url
     * @param requestBody
     * @param timeout           by milliseconds
     * @return
     */
    public static String postBody(String url,
                                  String requestBody,
                                  int timeout) {
        return doRequest("POST", url, requestBody, null, timeout);
    }

    /**
     * post
     *
     * @param url
     * @param requestBody
     * @return
     */
    public static String postBody(String url, String requestBody) {
        return doRequest("POST", url, requestBody, null, 1000);
    }

    // ---------------------- get ----------------------

    /**
     * get
     *
     * @param url
     * @param timeout       by milliseconds
     * @param headers
     * @return
     */
    public static String get(String url,
                             int timeout,
                             Map<String, String> headers) {
        return doRequest("POST", url, null, headers, timeout);
    }

    /**
     * get
     *
     * @param url
     * @param timeout       by milliseconds
     * @return
     */
    public static String get(String url, int timeout) {
        return doRequest("POST", url, null, null, timeout);
    }

    /**
     * get
     *
     * @param url
     * @return
     */
    public static String get(String url) {
        return doRequest("POST", url, null, null, 1000);
    }


}
