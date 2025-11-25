package com.example.financedashboard.config;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * HTTP客户端配置
 * 用于配置OkHttp客户端，调用新浪财经API
 */
@Configuration
public class HttpClientConfig {

    /**
     * 配置OkHttp客户端Bean
     * 设置连接超时、读写超时、连接池等参数
     */
    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)     // 连接超时15秒
                .readTimeout(30, TimeUnit.SECONDS)        // 读取超时30秒
                .writeTimeout(30, TimeUnit.SECONDS)       // 写入超时30秒
                .connectionPool(new ConnectionPool(10, 5, TimeUnit.MINUTES))  // 连接池：最多10个连接，空闲5分钟
                .retryOnConnectionFailure(true)           // 连接失败时自动重试
                .build();
    }
}