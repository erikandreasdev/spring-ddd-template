package com.CAMBIAME.shared.infrastructure;

import com.CAMBIAME.shared.application.UseCase;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Role;
import org.springframework.transaction.interceptor.MatchAlwaysTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

/**
 * Cableado de los casos de uso, para que la capa de aplicación no dependa de Spring:
 *
 * <ul>
 *   <li>registra como bean toda clase anotada con {@link UseCase};
 *   <li>ejecuta sus métodos en una transacción (propagación REQUIRED, rollback con
 *       RuntimeException y Error).
 * </ul>
 */
@Configuration(proxyBeanMethods = false)
@ComponentScan(
    basePackages = "com.CAMBIAME",
    useDefaultFilters = false,
    includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = UseCase.class))
class UseCaseConfig {

  // ROLE_INFRASTRUCTURE: así lo aplica el auto-proxy que Spring Boot registra para las
  // transacciones, sin necesidad de añadir el starter de AspectJ.
  @Bean
  @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
  static Advisor useCaseTransactionAdvisor(final BeanFactory beanFactory) {
    final TransactionInterceptor interceptor = new TransactionInterceptor();
    interceptor.setTransactionAttributeSource(new MatchAlwaysTransactionAttributeSource());
    // El TransactionManager se resuelve en la primera llamada, no al arrancar.
    interceptor.setBeanFactory(beanFactory);
    return new DefaultPointcutAdvisor(
        AnnotationMatchingPointcut.forClassAnnotation(UseCase.class), interceptor);
  }
}
