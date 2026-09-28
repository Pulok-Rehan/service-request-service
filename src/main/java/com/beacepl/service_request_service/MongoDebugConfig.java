package com.beacepl.service_request_service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class MongoDebugConfig {

    @Value("${spring.data.mongodb.uri:NOT_SET}")
    private String mongoUri;


    @Bean
    public CommandLineRunner testMongo(MongoTemplate mongoTemplate) {
        return args -> {
            System.out.println("--- MongoDB Debug Info ---");
            System.out.println("spring.data.mongodb.uri: " + mongoUri);
            System.out.println("Actual Connected DB: " + mongoTemplate.getDb().getName());
            System.out.println("---------------------------");
        };
    }
}