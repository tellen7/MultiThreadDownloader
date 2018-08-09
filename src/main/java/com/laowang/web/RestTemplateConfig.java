package com.laowang.web;

import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpResponse;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * @author wangyonghao
 */
@Component
public class RestTemplateConfig {
    /**
     * 采用apache提供的http库建立http连接，默认采用JDK的
     * @param
     * @return
     */
    @Bean
    public RestTemplate restTemplate(HttpComponentsClientHttpRequestFactory httpClientFactory) {
        RestTemplate restTemplate = new RestTemplate(httpClientFactory);
        return restTemplate;
    }

    @Bean
    HttpComponentsClientHttpRequestFactory httpClientFactory() {
        PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager();
        CloseableHttpClient httpClient = null;

        // 设置最大连接数
        manager.setMaxTotal(10);
        // 设置每个路由并发
        manager.setDefaultMaxPerRoute(5);

        ConnectionKeepAliveStrategy keepAliveStrategy = new ConnectionKeepAliveStrategy() {

            /*
             * 自定义keepAlive策略，如果响应头有timeout，则连接可以安全的保持空闲时间是timeout秒
             * 如果没有timeout，默认保持30秒
             */
            @Override
            public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
                HeaderElementIterator it = new BasicHeaderElementIterator(
                        response.headerIterator(HTTP.CONN_KEEP_ALIVE));
                while (it.hasNext()) {
                    HeaderElement element = it.nextElement();
                    String param = element.getName();
                    String value = element.getValue();
                    if (value != null && "timeout".equalsIgnoreCase(param)) {
                        return Long.parseLong(value) * 1000;
                    }
                }
                return 30 * 1000;
            }
        };

        httpClient = HttpClients.custom()
                .setConnectionManager(manager)
                .setKeepAliveStrategy(keepAliveStrategy)
                .setConnectionManagerShared(true)
                .build();
        HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory(
                httpClient);
        // 请求超时时间
        httpRequestFactory.setReadTimeout(30000);
        // 等待数据超时时间
        httpRequestFactory.setConnectTimeout(60000);
        httpRequestFactory.setConnectionRequestTimeout(30000);
        return httpRequestFactory;
    }
}
