package com.kodocode.api.config;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.kodocode.api.support.TestProperties;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class SecurityConfigurationValidatorTest {

    @Test
    void rejectsInsecureCookiesInProduction() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("prod");

        SecurityConfigurationValidator validator = new SecurityConfigurationValidator(TestProperties.create(), environment);

        assertThatThrownBy(validator::afterPropertiesSet)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cookies Secure");
    }
}
