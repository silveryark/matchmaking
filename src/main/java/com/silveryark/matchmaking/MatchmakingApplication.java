package com.silveryark.matchmaking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.silveryark")
public class MatchmakingApplication {

    public static void main(String[] args) {
        SpringApplication.run(MatchmakingApplication.class, args);
    }
}
