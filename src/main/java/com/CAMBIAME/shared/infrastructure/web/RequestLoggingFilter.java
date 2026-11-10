package com.CAMBIAME.shared.infrastructure.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Una línea INFO por petición. El traceId lo añade el patrón de log de Spring Boot. */
@Component
class RequestLoggingFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

  @Override
  protected void doFilterInternal(
      final HttpServletRequest request,
      final HttpServletResponse response,
      final FilterChain chain)
      throws ServletException, IOException {
    final long start = System.nanoTime();
    try {
      chain.doFilter(request, response);
    } finally {
      final long millis = (System.nanoTime() - start) / 1_000_000;
      log.info(
          "{} {} {} {}ms",
          request.getMethod(),
          request.getRequestURI(),
          response.getStatus(),
          millis);
    }
  }
}
