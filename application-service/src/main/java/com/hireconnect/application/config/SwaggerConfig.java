package com.hireconnect.application.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI applicationServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("HireConnect Application Service API")
                        .version("1.0")
                        .description("APIs for job applications, recruiter actions and analytics"));
    }
}