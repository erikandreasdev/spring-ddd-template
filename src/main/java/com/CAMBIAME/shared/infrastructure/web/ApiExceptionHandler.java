package com.CAMBIAME.shared.infrastructure.web;

import com.CAMBIAME.shared.domain.DomainException;
import com.CAMBIAME.shared.domain.NotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Traduce excepciones a ProblemDetail (RFC 9457) con el traceId de la petición. Es el único
 * lugar donde se registran los errores y donde se captura Exception de forma genérica.
 */
@RestControllerAdvice
class ApiExceptionHandler extends ResponseEntityExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

  @ExceptionHandler(NotFoundException.class)
  ProblemDetail handleNotFound(final NotFoundException exception) {
    return problem(HttpStatus.NOT_FOUND, exception.getMessage());
  }

  @ExceptionHandler(DomainException.class)
  ProblemDetail handleDomain(final DomainException exception) {
    return problem(HttpStatus.CONFLICT, exception.getMessage());
  }

  @ExceptionHandler({IllegalArgumentException.class, ConstraintViolationException.class})
  ProblemDetail handleBadRequest(final RuntimeException exception) {
    return problem(HttpStatus.BAD_REQUEST, exception.getMessage());
  }

  @ExceptionHandler(Exception.class)
  ProblemDetail handleUnexpected(final Exception exception) {
    log.error("Unexpected error", exception);
    return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error");
  }

  // Errores del propio Spring (validación, JSON mal formado, ruta inexistente...).
  @Override
  protected ResponseEntity<Object> createResponseEntity(
      final @Nullable Object body,
      final HttpHeaders headers,
      final HttpStatusCode statusCode,
      final WebRequest request) {
    Optional.ofNullable(body)
        .filter(ProblemDetail.class::isInstance)
        .map(ProblemDetail.class::cast)
        .ifPresent(ApiExceptionHandler::addTraceId);
    return super.createResponseEntity(body, headers, statusCode, request);
  }

  private static ProblemDetail problem(final HttpStatus status, final String detail) {
    final ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
    addTraceId(problem);
    return problem;
  }

  private static void addTraceId(final ProblemDetail problem) {
    Optional.ofNullable(MDC.get("traceId"))
        .ifPresent(traceId -> problem.setProperty("traceId", traceId));
  }
}
