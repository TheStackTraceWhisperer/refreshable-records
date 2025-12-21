package com.example;

import com.example.config.RefreshableMyConfigRecord;
import com.example.service.MyService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableConfigurationProperties(RefreshableMyConfigRecord.class)
public class RefreshableRecordsApplication {

    public static void main(String[] args) {
        SpringApplication.run(RefreshableRecordsApplication.class, args);
    }

    @Bean
    public CommandLineRunner demo(MyService myService) {
        return args -> {
            System.out.println("Running demo of refreshable records...");
            myService.performAction();
        };
    }
}
