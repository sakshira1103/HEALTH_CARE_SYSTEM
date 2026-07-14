package com.healthcare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Secure Healthcare Management System.
 *
 * Entry point for the HIPAA-oriented demo backend. Wires together:
 *  - OAuth2 / JWT authentication
 *  - Role-based access control
 *  - AOP-based audit logging
 *  - Field-level encryption
 *  - Redis caching
 *  - Micrometer/Prometheus monitoring
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
public class HealthcareApplication {

    public static void main(String[] args) {
        SpringApplication.run(HealthcareApplication.class, args);
    }
}
