package com.example.finance.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

/**
 * 网关配置类
 */
@Configuration
public class GatewayConfig {
    
    /**
     * 基于用户名的限流Key解析器
     * @return KeyResolver
     */
    @Bean("userKeyResolver")
    public KeyResolver userKeyResolver() {
        return exchange -> {
            // 从请求头中获取用户信息
            String username = exchange.getRequest().getHeaders().getFirst("X-User-Name");
            if (username == null) {
                // 如果没有用户信息，使用IP地址
                return exchange.getRequest().getRemoteAddress()
                        .map(address -> address.getAddress().getHostAddress());
            }
            return Mono.just(username);
        };
    }
    
    /**
     * 基于IP地址的限流Key解析器
     * @return KeyResolver
     */
    @Bean("ipKeyResolver")
    public KeyResolver ipKeyResolver() {
        return exchange -> exchange.getRequest().getRemoteAddress()
                .map(address -> address.getAddress().getHostAddress());
    }
    
    /**
     * 基于请求路径的限流Key解析器
     * @return KeyResolver
     */
    @Bean("pathKeyResolver")
    public KeyResolver pathKeyResolver() {
        return exchange -> Mono.just(exchange.getRequest().getPath().value());
    }
}