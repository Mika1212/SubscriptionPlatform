package org.mika1212;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class SubscriptionPlatformApplication {
    public static void main(String[] args) {
        SpringApplication.run(SubscriptionPlatformApplication.class, args);
    }
}
