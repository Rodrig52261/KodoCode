package com.kodocode.api;

import com.kodocode.api.config.ApplicationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@EnableJpaAuditing
@EnableConfigurationProperties(ApplicationProperties.class)
public class KodoCodeApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(KodoCodeApiApplication.class, args);
    }
}
