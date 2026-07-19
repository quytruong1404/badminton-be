package com.quy.badmintonbe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BadmintonBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(BadmintonBeApplication.class, args);
    }

}
