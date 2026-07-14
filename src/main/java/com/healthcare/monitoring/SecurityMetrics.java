package com.healthcare.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

/**
 * Custom application metrics exposed at /actuator/prometheus.
 * These are the numbers a security team would actually want on a dashboard:
 * login failure rate, access-denial rate, and active 2FA challenges.
 * Wire these counters into AuthService / GlobalExceptionHandler as needed.
 */
@Component
public class SecurityMetrics {

    private final Counter loginSuccessCounter;
    private final Counter loginFailureCounter;
    private final Counter accessDeniedCounter;
    private final Counter twoFactorChallengeCounter;

    public SecurityMetrics(MeterRegistry registry) {
        this.loginSuccessCounter = Counter.builder("healthcare.auth.login.success")
                .description("Number of successful login attempts")
                .register(registry);

        this.loginFailureCounter = Counter.builder("healthcare.auth.login.failure")
                .description("Number of failed login attempts")
                .register(registry);

        this.accessDeniedCounter = Counter.builder("healthcare.auth.access_denied")
                .description("Number of 403 access-denied responses")
                .register(registry);

        this.twoFactorChallengeCounter = Counter.builder("healthcare.auth.2fa.challenge")
                .description("Number of two-factor authentication challenges issued")
                .register(registry);
    }

    public void recordLoginSuccess() {
        loginSuccessCounter.increment();
    }

    public void recordLoginFailure() {
        loginFailureCounter.increment();
    }

    public void recordAccessDenied() {
        accessDeniedCounter.increment();
    }

    public void recordTwoFactorChallenge() {
        twoFactorChallengeCounter.increment();
    }
}
