package com.xuanjia.airouter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling 
public class AiRouterApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiRouterApplication.class, args);
    }

}
