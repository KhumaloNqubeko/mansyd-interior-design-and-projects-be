package com.carpenter.business.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    OpenAPI carpenterApi() {
        return new OpenAPI().info(new Info()
                .title("Carpentry Business Management API")
                .version("v1")
                .description("Session-authenticated API for carpentry business operations."));
    }
}
