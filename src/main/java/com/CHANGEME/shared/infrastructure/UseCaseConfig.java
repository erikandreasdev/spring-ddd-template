package com.CHANGEME.shared.infrastructure;

import com.CHANGEME.shared.application.UseCase;
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
 * Use case wiring, so that the application layer does not depend on Spring:
 *
 * <ul>
 *   <li>registers every class annotated with {@link UseCase} as a bean;
 *   <li>runs its methods in a transaction (REQUIRED propagation, rollback on
 *       RuntimeException and Error).
 * </ul>
 */
@Configuration(proxyBeanMethods = false)
@ComponentScan(
    basePackages = "com.CHANGEME",
    useDefaultFilters = false,
    includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = UseCase.class))
class UseCaseConfig {

  // ROLE_INFRASTRUCTURE: this way the auto-proxy that Spring Boot registers for transactions
  // applies it, without having to add the AspectJ starter.
  @Bean
  @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
  static Advisor useCaseTransactionAdvisor(final BeanFactory beanFactory) {
    final TransactionInterceptor interceptor = new TransactionInterceptor();
    interceptor.setTransactionAttributeSource(new MatchAlwaysTransactionAttributeSource());
    // The TransactionManager is resolved on the first call, not at startup.
    interceptor.setBeanFactory(beanFactory);
    return new DefaultPointcutAdvisor(
        AnnotationMatchingPointcut.forClassAnnotation(UseCase.class), interceptor);
  }
}
