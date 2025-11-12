package com.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OrderSagaOrchestratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderSagaOrchestratorApplication.class, args);
    }

}
