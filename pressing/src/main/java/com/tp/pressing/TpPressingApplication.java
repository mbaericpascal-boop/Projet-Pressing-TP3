package com.tp.pressing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class TpPressingApplication {

    public static void main(String[] args) {
        SpringApplication.run(TpPressingApplication.class, args);
    }
}
