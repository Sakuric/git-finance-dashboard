package com.example.finance.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * 金融平台配置中心启动类
 */
@EnableConfigServer
@EnableEurekaClient
@SpringBootApplication
public class FinanceConfigApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinanceConfigApplication.class, args);
        System.out.println("金融平台配置中心启动成功！");
    }
}