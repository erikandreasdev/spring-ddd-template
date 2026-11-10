package com.CHANGEME;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;

// Single WireMock server for all integration tests, like TestcontainersConfig.
// Add it with the first adapter to an external service:
//   1. test dependency org.wiremock:wiremock-standalone (3.13.2 or the latest 3.x)
//   2. @Import({TestcontainersConfig.class, WireMockConfig.class}) in IntegrationTest
// Each adapter points its base URL here; no profile is needed.
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
