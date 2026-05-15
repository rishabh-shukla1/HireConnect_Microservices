package com.hireconnect.interview;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class InterviewMicroserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(InterviewMicroserviceApplication.class, args);
	}

}
