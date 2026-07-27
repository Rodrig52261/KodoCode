package com.kodocode.api.email;

import java.time.Duration;

final class EmailJsRateLimiter {
    private final long intervalNanos;
    private long nextRequestNanos;

    EmailJsRateLimiter(Duration minimumInterval) {
        this.intervalNanos = minimumInterval == null ? 0 : Math.max(0, minimumInterval.toNanos());
    }

    synchronized void acquire() {
        long now = System.nanoTime();
        long waitNanos = nextRequestNanos - now;
        if (waitNanos > 0) {
            try {
                long millis = waitNanos / 1_000_000;
                int nanos = (int) (waitNanos % 1_000_000);
                Thread.sleep(millis, nanos);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new EmailJsDeliveryException("Envio interrompido antes de chamar o EmailJS.", exception);
            }
        }
        nextRequestNanos = System.nanoTime() + intervalNanos;
    }
}
