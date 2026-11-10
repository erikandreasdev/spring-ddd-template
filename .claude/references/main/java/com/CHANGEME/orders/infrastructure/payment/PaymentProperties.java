package com.CHANGEME.orders.infrastructure.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

// Adapter properties: a validated record, never @Value. If a value is missing, the application
// does not start. In application.yml:
//
//   payments:
//     base-url: ${PAYMENTS_BASE_URL}
//     api-key: ${PAYMENTS_API_KEY}
//     timeout: 5s
@Validated
@ConfigurationProperties("payments")
record PaymentProperties(@NotNull URI baseUrl, @NotBlank String apiKey, @NotNull Duration timeout) {}
