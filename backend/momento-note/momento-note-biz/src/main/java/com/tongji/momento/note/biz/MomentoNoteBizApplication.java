package com.tongji.momento.note.biz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@MapperScan("com.tongji.momento.note.biz.domain.mapper")
@EnableFeignClients(basePackages = "com.tongji.momento")
public class MomentoNoteBizApplication {

    public static void main(String[] args) {
        SpringApplication.run(MomentoNoteBizApplication.class, args);
    }

}
