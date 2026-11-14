package com.agri.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AgriPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgriPlatformApplication.class, args);
        System.out.println("==================================================================");
        System.out.println(" 农产品直播电商产销对接平台 (SpringBoot+Vue) 后端服务已成功启动！");
        System.out.println(" API 接口基础地址: http://localhost:8080/api");
        System.out.println(" 内置 H2 控制台地址: http://localhost:8080/h2-console");
        System.out.println("==================================================================");
    }
}
