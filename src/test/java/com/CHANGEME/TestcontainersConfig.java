package com.CHANGEME;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfig {

  // CHANGEME: database. Container for your engine, with the same version as production.
  @Bean
  @ServiceConnection
  PostgreSQLContainer database() {
    return new PostgreSQLContainer("postgres:17");
  }
}
