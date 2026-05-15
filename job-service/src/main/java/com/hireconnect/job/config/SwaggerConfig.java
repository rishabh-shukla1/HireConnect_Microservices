package com.hireconnect.job.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI jobServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("HireConnect Job Service API")
                        .description("APIs for managing job postings, recruiter jobs, search and job status management")
                        .version("1.0")
                        .contact(new Contact()
                                .name("HireConnect Team")
                                .email("support@hireconnect.com"))
                        .license(new License()
                                .name("Apache 2.0")));
    }
}