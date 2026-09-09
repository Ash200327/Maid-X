package com.example.maidmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class MaidManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MaidManagerApplication.class, args);
    }
}
