package com.example.finance.registry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * 金融平台注册中心启动类
 */
@EnableEurekaServer
@SpringBootApplication
public class FinanceRegistryApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinanceRegistryApplication.class, args);
        System.out.println("金融平台注册中心启动成功！");
    }
}