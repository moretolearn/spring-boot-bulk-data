package com.moretolearn;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootApplication
public class SpringBootBulkDataApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootBulkDataApplication.class, args);
	}
	
	@Bean
	ObjectMapper objectMapper() {
		return new ObjectMapper();
	}
	
	@Bean
    Executor streamingExecutor() {
        return Executors.newSingleThreadExecutor();
    }

}
