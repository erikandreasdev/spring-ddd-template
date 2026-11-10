package com.CAMBIAME;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;

// Servidor WireMock único para todos los tests de integración, como TestcontainersConfig.
// Se añade con el primer adaptador a un servicio externo:
//   1. dependencia de test org.wiremock:wiremock-standalone (3.13.2 o la última 3.x)
//   2. @Import({TestcontainersConfig.class, WireMockConfig.class}) en IntegrationTest
// Cada adaptador apunta su URL base aquí; no hace falta ningún perfil.
@TestConfiguration(proxyBeanMethods = false)
class WireMockConfig {

  @Bean(destroyMethod = "stop")
  WireMockServer wireMockServer() {
    final WireMockServer server = new WireMockServer(options().dynamicPort());
    server.start();
    return server;
  }

  @Bean
  DynamicPropertyRegistrar externalServicesProperties(final WireMockServer server) {
    return registry -> {
      registry.add("payments.base-url", server::baseUrl);
      registry.add("payments.api-key", () -> "test-api-key");
      registry.add("payments.timeout", () -> "2s");
    };
  }
}
