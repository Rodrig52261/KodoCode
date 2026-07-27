package com.kodocode.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI kodoCodeOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Kodo Code API")
                .description("API REST versionada do site institucional e painel administrativo.")
                .version("v1"));
    }
}

