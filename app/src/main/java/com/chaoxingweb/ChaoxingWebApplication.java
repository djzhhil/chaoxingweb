package com.chaoxingweb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Chaoxing Web 应用启动类
 */
@SpringBootApplication
@EnableScheduling
public class ChaoxingWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChaoxingWebApplication.class, args);
    }
}
