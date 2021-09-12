package lava.watcher.util;

import lava.watcher.constant.HTTPRequestTypeEnum;
import com.google.gson.reflect.TypeToken;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.*;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Auther: lava
 * @Date: 2021/8/16 15:54
 * @Description: http客户端，支持GET和POST，请求与响应体为JSON
 */
@Slf4j
public class HttpJsonUtil {
    private static final String SUCCESS = "success";
    private static final String FAIL = "fail";

    private static final int maxTotal = 8;
    private static final int maxPerRoute = 2;
    private static final int connectTimeout = 4000;
    private static final int requestTimeout = 2000;
    private static final int socketTimeout = 4000;

    private static final CloseableHttpClient httpClient;

    static {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        // 总连接数量
        connectionManager.setMaxTotal(maxTotal);
        // 每个服务的最大连接数量
        connectionManager.setDefaultMaxPerRoute(maxPerRoute);
        // setConnectTimeout: 建立连接的超时时间
        // setConnectionRequestTimeout: 从连接池中拿连接的等待超时时间
        // setSocketTimeout: 发出请求后等待对端应答的超时时间
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(connectTimeout).
                setConnectionRequestTimeout(requestTimeout)
                .setSocketTimeout(socketTimeout).build();
        // 默认重试实现，最多三次
        HttpRequestRetryHandler retryHandler = new StandardHttpRequestRetryHandler();
        // 设置HTTP头
        List<Header> defaultHeaders = new ArrayList<>();
        defaultHeaders.add(new BasicHeader("Content-Type", "application/json;charset=UTF-8"));
        defaultHeaders.add(new BasicHeader("Accept", "application/json"));
        // 构建httpClient
        httpClient = HttpClients.custom().setConnectionManager(connectionManager).setDefaultRequestConfig(requestConfig)
                .setRetryHandler(retryHandler).setDefaultHeaders(defaultHeaders).build();
    }

    private HttpJsonUtil() {
    }

    /**
     * @Description 发送请求
     * @Date 2021/8/18 2:17 下午
     * @Param [httpType, path, param] Http方法，请求路径，参数
     * @return com.bytedance.kunlun.connector.KunlunResponse
     **/
    public static HTTPResponse send(HTTPRequestTypeEnum httpType, String path, Object param, Object body, List<Header> headers) {
        HTTPResponse response = null;
        log.info("[HttpJsonUtil] http send, path:{}, param:{}, body:{}", path, JsonUtil.serialize(param), JsonUtil.serialize(body));
        switch (httpType) {
            case GET:
                response = doGet(path, param, headers);
                break;
            case POST:
                response = doPost(path, param, body, headers);
                break;
            default:
                throw new UnsupportedOperationException();
        }
        log.info("[HttpJsonUtil] http received, httpCode:{}, data:{}, exception:{}", response.getHttpCode(), response.getJson(), Objects.isNull(response.getCause()) ? null : response.getCause().getMessage());
        return response;
    }

    private static HTTPResponse doGet(@NonNull String path, Object param, List<Header> headers){
        final HTTPResponse response = new HTTPResponse();
        String paramStr = "";
        if (Objects.nonNull(param)){
            try {
                Map<String, String> paramMap = JsonUtil.parseCollection(JsonUtil.serialize(param), new TypeToken<Map<String, String>>(){});
                List<NameValuePair> paramList = paramMap.entrySet().stream().
                        map(entry -> new BasicNameValuePair(entry.getKey(), entry.getValue())).collect(Collectors.toList());
                paramStr = EntityUtils.toString(new UrlEncodedFormEntity(paramList, Consts.UTF_8));
            } catch (IOException e) {
                response.setCause(e);
                response.setHttpCode(0);
                response.setMessage(FAIL);
                return response;
            }
        }
        HttpGet httpGet = new HttpGet(path + "?" + paramStr);
        if (Objects.nonNull(headers)){
            headers.forEach(httpGet::setHeader);
        }
        CloseableHttpResponse rsp = null;
        int statusCode = 0;
        String respHtml = null;
        try {
            rsp = httpClient.execute(httpGet);
            statusCode = rsp.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK){
                HttpEntity respEnt = rsp.getEntity();
                respHtml = EntityUtils.toString(respEnt, "UTF-8");
            }
        } catch (IOException e) {
            response.setMessage(FAIL);
            response.setCause(e);
            response.setHttpCode(statusCode);
            return response;
        } finally {
            try {
                if (Objects.nonNull(rsp)){
                    rsp.close();
                }
            } catch (IOException ignored) {
            }
        }
        response.setHttpCode(statusCode);
        response.setJson(respHtml);
        response.setMessage(SUCCESS);
        return response;
    }

    private static HTTPResponse doPost(@NonNull String path, Object param, Object body, List<Header> headers){
        final HTTPResponse response = new HTTPResponse();
        String paramStr = "";
        if (Objects.nonNull(param)){
            try {
                Map<String, String> paramMap = JsonUtil.parseCollection(JsonUtil.serialize(param), new TypeToken<Map<String, String>>(){});
                List<NameValuePair> paramList = paramMap.entrySet().stream().
                        map(entry -> new BasicNameValuePair(entry.getKey(), entry.getValue())).collect(Collectors.toList());
                paramStr = EntityUtils.toString(new UrlEncodedFormEntity(paramList, Consts.UTF_8));
            } catch (IOException e) {
                response.setCause(e);
                response.setHttpCode(0);
                response.setMessage(FAIL);
                return response;
            }
        }
        HttpPost httpPost = new HttpPost(path + "?" + paramStr);
        if (Objects.nonNull(headers)){
            headers.forEach(httpPost::setHeader);
        }
        if (Objects.nonNull(body)){
            String jsonStr = JsonUtil.serialize(body);
            StringEntity entity = new StringEntity(jsonStr, ContentType.APPLICATION_JSON);
            httpPost.setEntity(entity);
        }
        CloseableHttpResponse rsp = null;
        int statusCode = 0;
        String respHtml = null;
        try {
            rsp = httpClient.execute(httpPost);
            statusCode = rsp.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK){
                HttpEntity respEnt = rsp.getEntity();
                respHtml = EntityUtils.toString(respEnt, "UTF-8");
            }
        } catch (IOException e) {
            response.setMessage(FAIL);
            response.setCause(e);
            response.setHttpCode(statusCode);
            return response;
        } finally {
            try {
                if (Objects.nonNull(rsp)){
                    rsp.close();
                }
            } catch (IOException ignored) {
            }
        }
        response.setHttpCode(statusCode);
        response.setJson(respHtml);
        response.setMessage(SUCCESS);
        return response;
    }

    @Data
    public static class HTTPResponse {
        private int httpCode;
        private String message;
        private Throwable cause;
        private String json;

        public boolean isSuccess(){
            return SUCCESS.equals(message) && HttpStatus.SC_OK == httpCode;
        }
    }
}

