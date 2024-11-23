package com.tongji.momento.user.relation.biz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@MapperScan("com.tongji.momento.user.relation.biz.domain.mapper")
@EnableFeignClients(basePackages = "com.tongji.momento")
public class MomentoUserRelationBizApplication {

    public static void main(String[] args) {
        SpringApplication.run(MomentoUserRelationBizApplication.class, args);
    }

}
