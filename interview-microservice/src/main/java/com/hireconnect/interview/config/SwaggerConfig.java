package com.hireconnect.interview.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI interviewServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("HireConnect Interview Service API")
                        .version("1.0")
                        .description("Interview scheduling, rescheduling, confirmation and cancellation APIs"));
    }
}