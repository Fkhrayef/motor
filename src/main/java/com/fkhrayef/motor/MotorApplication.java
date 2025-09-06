package com.fkhrayef.motor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MotorApplication {

    public static void main(String[] args) {
        SpringApplication.run(MotorApplication.class, args);
    }

}
