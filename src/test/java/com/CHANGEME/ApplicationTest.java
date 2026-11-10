package com.CHANGEME;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

@IntegrationTest
class ApplicationTest {

  @Autowired private ApplicationContext context;

  @Test
  void givenTheApplication_whenContextStarts_thenUseCaseTransactionsAreConfigured() {
    // when
    final boolean advisorRegistered = context.containsBean("useCaseTransactionAdvisor");

    // then
    assertThat(advisorRegistered).isTrue();
  }
}
