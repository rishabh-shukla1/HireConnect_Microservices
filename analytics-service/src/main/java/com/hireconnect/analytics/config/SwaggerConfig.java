package com.hireconnect.analytics.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI analyticsOpenAPI() {

        return new OpenAPI()
                .info(new Info()
                        .title("HireConnect Analytics Service API")
                        .version("1.0.0")
                        .description("Analytics APIs for recruiter dashboards, hiring metrics, and platform-wide reporting.")
                        .contact(new Contact()
                                .name("HireConnect Team")
                                .email("support@hireconnect.com")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8088")
                                .description("Local Analytics Service")
                ));
    }
}