package com.CHANGEME;

import org.springframework.boot.SpringApplication;

/** Runs the application locally with the Testcontainers database: mvn spring-boot:test-run */
public class TestApplication {

  public static void main(final String[] args) {
    SpringApplication.from(Application::main).with(TestcontainersConfig.class).run(args);
  }
}
