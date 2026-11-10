package com.CAMBIAME.orders.infrastructure.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

// Propiedades de un adaptador: record validado, nunca @Value. Si falta un valor, la aplicación
// no arranca. En application.yml:
//
//   payments:
//     base-url: ${PAYMENTS_BASE_URL}
//     api-key: ${PAYMENTS_API_KEY}
//     timeout: 5s
@Validated
@ConfigurationProperties("payments")
record PaymentProperties(@NotNull URI baseUrl, @NotBlank String apiKey, @NotNull Duration timeout) {}
