package com.tongji.momento.user.biz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@MapperScan("com.tongji.momento.user.biz.domain.mapper")
@EnableFeignClients(basePackages = "com.tongji.momento")
public class MomentoUserBizApplication {

    public static void main(String[] args) {
        SpringApplication.run(MomentoUserBizApplication.class, args);
    }
}
