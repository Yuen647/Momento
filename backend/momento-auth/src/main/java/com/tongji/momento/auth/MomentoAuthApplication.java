package com.tongji.momento.auth;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.tongji.momento")
public class MomentoAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(MomentoAuthApplication.class, args);
    }

}
