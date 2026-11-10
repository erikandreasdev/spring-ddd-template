package com.CAMBIAME.shared.infrastructure;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Hora del sistema en UTC. Dominio y casos de uso la reciben inyectada, nunca con now() directo. */
@Configuration(proxyBeanMethods = false)
class ClockConfig {

  @Bean
  Clock clock() {
    return Clock.systemUTC();
  }
}
