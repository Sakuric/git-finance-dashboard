package com.example.finance.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * 金融平台API网关启动类
 */
@EnableEurekaClient
@SpringBootApplication
public class FinanceGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinanceGatewayApplication.class, args);
        System.out.println("金融平台API网关启动成功！");
    }
}