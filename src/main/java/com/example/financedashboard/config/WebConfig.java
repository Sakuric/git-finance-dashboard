package com.example.financedashboard.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    // JWT验证已经集成到Spring Security中，不再需要单独的拦截器
}
