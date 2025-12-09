package com.example.financedashboard.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 大模型配置类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "llm")
public class LLMConfig {
    private String provider;
    private String apiKey;
    private String baseUrl;
    private String model;
    private Integer timeout;
}
