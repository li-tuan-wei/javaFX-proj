package ldh.common.testui.util;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by ldh on 2019/2/28.
 */
public class HttpClientCustomer {

    private final static Logger LOGGER = Logger.getLogger(HttpClientUtil.class.getSimpleName());

    // 时间未毫秒
    private RequestConfig requestConfig = RequestConfig.custom()
            .setSocketTimeout(6000000)  // 请求获取数据的超时时间，单位毫秒。 如果访问一个接口，多少时间内无法返回数据，就直接放弃此次调用。
            .setConnectTimeout(15000)  // 设置连接超时时间，单位毫秒
            .setConnectionRequestTimeout(1000000) // 设置从connect Manager获取Connection 超时时间，单位毫秒。这个属性是新加的属性，因为目前版本是可以共享连接池的
            .build();

    private CloseableHttpClient closeableHttpClient = null;


    public HttpClientCustomer(){
        buildHttpClient();
    }

    /**
     * 发送 post请求
     * @param httpUrl 地址
     * @param params 参数(格式:key1=value1&key2=value2)
     */
    public String sendHttpPost(String httpUrl, String params) {
        HttpPost httpPost = new HttpPost(httpUrl);// 创建httpPost
        try {
            //设置参数
            StringEntity stringEntity = new StringEntity(params, "UTF-8");
            stringEntity.setContentType("application/x-www-form-urlencoded");
            httpPost.setEntity(stringEntity);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sendHttp(httpPost);
    }

    /**
     * 发送 post请求
     * @param httpUrl 地址
     * @param paramMap 参数
     */
    public String sendHttpPost(String httpUrl, Map<String, Object> headerMap, Map<String, Object> paramMap) {
        LOGGER.info("HTTP API: url:" + httpUrl);
        HttpPost httpPost = new HttpPost(httpUrl);// 创建httpPost
        for (Map.Entry<String, Object> header : headerMap.entrySet()) {
            httpPost.addHeader(header.getKey(), header.getValue().toString());
        }
        LOGGER.info("HTTP API: header:" + JsonUtil.toJson(headerMap));
        // 创建参数队列
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        for (String key : paramMap.keySet()) {
            nameValuePairs.add(new BasicNameValuePair(key, paramMap.get(key).toString()));
        }
        LOGGER.info("HTTP API: param:" + JsonUtil.toJson(paramMap));
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sendHttp(httpPost);
    }

    public String sendHttpPost(String httpUrl, Map<String, Object> headerMap, Map<String, Object> paramMap, String body, String contentType) {
        LOGGER.info("  HTTP API: url:" + httpUrl);
        HttpPost httpPost = new HttpPost(httpUrl);// 创建httpPost
        for (Map.Entry<String, Object> header : headerMap.entrySet()) {
            httpPost.addHeader(header.getKey(), header.getValue().toString());
        }
        LOGGER.info("  HTTP API: header:" + JsonUtil.toJson(headerMap));
        // 创建参数队列
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        for (String key : paramMap.keySet()) {
            nameValuePairs.add(new BasicNameValuePair(key, paramMap.get(key).toString()));
        }
        LOGGER.info("  HTTP API: param:" + JsonUtil.toJson(paramMap));
        try {
            //设置参数
            StringEntity stringEntity = new StringEntity(body, "UTF-8");
            LOGGER.info("  HTTP API: body:" + stringEntity);
            LOGGER.info("  HTTP API: contentType:" + contentType);
            stringEntity.setContentType(contentType);
            httpPost.setEntity(stringEntity);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sendHttp(httpPost);
    }

    /**
     * 发送 post请求（带文件）
     * @param httpUrl 地址
     * @param maps 参数
     * @param fileLists 附件
     */
    public String sendHttpPost(String httpUrl, Map<String, String> maps, List<File> fileLists) {
        HttpPost httpPost = new HttpPost(httpUrl);// 创建httpPost
        MultipartEntityBuilder meBuilder = MultipartEntityBuilder.create();
        for (String key : maps.keySet()) {
            meBuilder.addPart(key, new StringBody(maps.get(key), ContentType.TEXT_PLAIN));
        }
        for(File file : fileLists) {
            FileBody fileBody = new FileBody(file);
            meBuilder.addPart("files", fileBody);
        }
        HttpEntity reqEntity = meBuilder.build();
        httpPost.setEntity(reqEntity);
        return sendHttp(httpPost);
    }

    /**
     * 发送 get请求
     * @param httpUrl
     */
    public String sendHttpGet(String httpUrl) {
        HttpGet httpGet = new HttpGet(httpUrl);// 创建get请求
        return sendHttp(httpGet);
    }

    public String sendHttpGet(String httpUrl, Map<String, Object> headerMap, Map<String, Object> paramMap) {
        RequestBuilder requestBuilder = RequestBuilder.get().setUri(httpUrl);
        for (String key : paramMap.keySet()) {
            requestBuilder.addParameter(new BasicNameValuePair(key, paramMap.get(key).toString()));
        }
        for (Map.Entry<String, Object> header : headerMap.entrySet()) {
            requestBuilder.addHeader(header.getKey(), header.getValue().toString());
        }
        return sendHttp(requestBuilder.build());
    }

    public String sendHttpGet(String httpUrl, Map<String, Object> headerMap, String body, String contentType) {
        RequestBuilder requestBuilder = RequestBuilder.get().setUri(httpUrl);
        for (Map.Entry<String, Object> header : headerMap.entrySet()) {
            requestBuilder.addHeader(header.getKey(), header.getValue().toString());
        }
        StringEntity stringEntity = new StringEntity(body, "UTF-8");
        stringEntity.setContentType(contentType);
        requestBuilder.setEntity(stringEntity);
        return sendHttp(requestBuilder.build());
    }

    /**
     * 发送Get请求
     * @param httpUriRequest
     * @return
     */
    private String sendHttp(HttpUriRequest httpUriRequest) {
        CloseableHttpResponse response = null;
        HttpEntity entity = null;
        String responseContent = null;
        try {
            // 创建cookie store的本地实例
            CookieStore cookieStore = new BasicCookieStore();
            // 创建HttpClient上下文
            HttpClientContext context = HttpClientContext.create();
            context.setCookieStore(cookieStore);

            if (httpUriRequest instanceof HttpRequestBase) {
                ((HttpRequestBase)httpUriRequest).setConfig(requestConfig);
            }

            // 执行请求
            response = closeableHttpClient.execute(httpUriRequest);
            entity = response.getEntity();
            responseContent = EntityUtils.toString(entity, "UTF-8");
            EntityUtils.consume(entity);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                // 关闭连接,释放资源
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return responseContent;
    }

    private void buildHttpClient() {
        // 创建cookie store的本地实例
        CookieStore cookieStore = new BasicCookieStore();
        // 创建HttpClient上下文
        HttpClientContext context = HttpClientContext.create();
        context.setCookieStore(cookieStore);

        // 创建默认的httpClient实例.
        closeableHttpClient =  HttpClients.custom().setDefaultRequestConfig(requestConfig)
                .setDefaultCookieStore(cookieStore).build();
    }

    public void close() {
        try {
            if (closeableHttpClient != null) {
                closeableHttpClient.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
