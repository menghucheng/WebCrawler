/*
 *Copyright (c) 2015 Andrew-Wang. 
 *
 *Licensed under the Apache License, Version 2.0 (the "License");
 *you may not use this file except in compliance with the License.
 *You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing, software
 *distributed under the License is distributed on an "AS IS" BASIS,
 *WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *See the License for the specific language governing permissions and
 *limitations under the License.
 */
package edu.xiyou.fruits.WebCrawler.net;

import edu.xiyou.fruits.WebCrawler.parser.CrawlDatum;
import edu.xiyou.fruits.WebCrawler.utils.Config;
import org.apache.http.client.CookieStore;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.*;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.cookie.BasicClientCookie;

import java.util.Map;

/**
 * 用于生成HttpClient实例
 * Created by andrew on 15-5-21.
 */
public class HttpClientGenerator {
    private PoolingHttpClientConnectionManager manager;

    public HttpClientGenerator(){
        Registry<ConnectionSocketFactory> r = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", SSLConnectionSocketFactory.getSocketFactory())
                .build();
        manager = new PoolingHttpClientConnectionManager(r);
        manager.setDefaultMaxPerRoute(100);
    }

    public HttpClientGenerator setPoolSize(int size){
        manager.setMaxTotal(size);
        return this;
    }

    public CloseableHttpClient getClient(CrawlDatum datum) {
        return generateClient(datum, null);
    }

    public CloseableHttpClient getClient(CrawlDatum datum, Map<String, String> cookies){
        return generateClient(datum, cookies);
    }

    private CloseableHttpClient generateClient(CrawlDatum datum, Map<String, String> cookies){
        if (datum == null){
            return null;
        }

        HttpClientBuilder builder = HttpClients.custom().setConnectionManager(manager);
        builder.setUserAgent(Config.USER_AGENT);
        SocketConfig socketConfig = SocketConfig.custom().setSoKeepAlive(true).setTcpNoDelay(true).build();

        builder.setDefaultSocketConfig(socketConfig);
        builder.setRetryHandler(new DefaultHttpRequestRetryHandler(Config.RETRY, true));
        builder.setRedirectStrategy(new DefaultRedirectStrategy());
        if (cookies != null){
            generateCookie(builder, cookies);
        }
        return builder.build();
    }

    public void generateCookie(HttpClientBuilder builder, Map<String, String> cookies){
        CookieStore cookieStore = new BasicCookieStore();
        for (Map.Entry<String, String> entry : cookies.entrySet()){
            BasicClientCookie cookie = new BasicClientCookie(entry.getKey(), entry.getValue());
            cookieStore.addCookie(cookie);
        }

        builder.setDefaultCookieStore(cookieStore);
    }
}
