package com.CAMBIAME;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfig {

  // CAMBIAME: base de datos. Contenedor del motor que uses, con la misma versión que producción.
  @Bean
  @ServiceConnection
  PostgreSQLContainer database() {
    return new PostgreSQLContainer("postgres:17");
  }
}
