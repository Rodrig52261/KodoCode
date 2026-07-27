package com.kodocode.api.lead;

import com.kodocode.api.config.ApplicationProperties;
import com.kodocode.api.shared.web.BusinessException;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ContactRateLimitService {

    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();
    private final int limit;
    private final Clock clock;

    @Autowired
    public ContactRateLimitService(ApplicationProperties properties) {
        this(properties.contact().rateLimitPerHour(), Clock.systemUTC());
    }

    ContactRateLimitService(int limit, Clock clock) {
        this.limit = limit;
        this.clock = clock;
    }

    public void check(String ipAddress, String email) {
        checkKey("ip:" + safe(ipAddress));
        checkKey("email:" + safe(email));
    }

    private void checkKey(String key) {
        Instant now = clock.instant();
        Window result = windows.compute(key, (ignored, current) -> {
            if (current == null || !current.startedAt().plus(1, ChronoUnit.HOURS).isAfter(now)) {
                return new Window(now, 1);
            }
            return new Window(current.startedAt(), current.count() + 1);
        });
        if (result.count() > limit) {
            throw new BusinessException("Muitas mensagens enviadas. Aguarde antes de tentar novamente.", HttpStatus.TOO_MANY_REQUESTS);
        }
        if (windows.size() > 10_000) {
            windows.entrySet().removeIf(entry -> entry.getValue().startedAt().plus(2, ChronoUnit.HOURS).isBefore(now));
        }
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "unknown" : value;
    }

    private record Window(Instant startedAt, int count) {}
}
