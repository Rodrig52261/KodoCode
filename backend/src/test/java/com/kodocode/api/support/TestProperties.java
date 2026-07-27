package com.kodocode.api.support;

import com.kodocode.api.config.ApplicationProperties;
import java.time.Duration;
import java.util.List;

public final class TestProperties {

    private TestProperties() {}

    public static ApplicationProperties create() {
        return new ApplicationProperties(
                new ApplicationProperties.Jwt(
                        "test-secret-with-at-least-thirty-two-characters",
                        Duration.ofMinutes(15),
                        Duration.ofDays(7),
                        "kodocode-test"),
                new ApplicationProperties.Cookie(false, "Lax", ""),
                new ApplicationProperties.Cors(List.of("http://localhost:3000")),
                new ApplicationProperties.Security(3, Duration.ofMinutes(15), 2),
                new ApplicationProperties.BootstrapAdmin(false, "", "", ""),
                new ApplicationProperties.Contact(Duration.ofSeconds(3), Duration.ofMinutes(10), 5),
                new ApplicationProperties.Email(false, "",
                        new ApplicationProperties.EmailJs(
                                "https://api.emailjs.com/api/v1.0/email/send",
                                "", "", "", "", "", Duration.ofMillis(1100))));
    }
}
