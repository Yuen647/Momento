package com.tongji.momento.count.biz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.tongji.momento.count.biz.domain.mapper")
public class MomentoCountBizApplication {

    public static void main(String[] args) {
        SpringApplication.run(MomentoCountBizApplication.class, args);
    }

}
