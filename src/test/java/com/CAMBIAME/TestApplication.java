package com.CAMBIAME;

import org.springframework.boot.SpringApplication;

/** Arranca la aplicación en local con la base de datos de Testcontainers: mvn spring-boot:test-run */
public class TestApplication {

  public static void main(final String[] args) {
    SpringApplication.from(Application::main).with(TestcontainersConfig.class).run(args);
  }
}
