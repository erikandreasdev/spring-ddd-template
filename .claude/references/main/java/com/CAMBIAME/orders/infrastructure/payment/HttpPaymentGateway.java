package com.CAMBIAME.orders.infrastructure.payment;

import com.CAMBIAME.orders.application.PaymentDeclinedException;
import com.CAMBIAME.orders.application.PaymentGateway;
import com.CAMBIAME.orders.domain.OrderId;
import com.CAMBIAME.shared.domain.Money;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

// Adaptador de un puerto de salida hacia un servicio HTTP externo, con RestClient.
// Necesita spring-boot-starter-restclient: el RestClient.Builder de Spring Boot propaga el
// traceId y registra métricas. URL base y timeouts salen de PaymentProperties.
// Traduce las respuestas a la semántica del puerto: 4xx es un rechazo de negocio, el resto un
// fallo técnico. No registra en log: el error se registra una sola vez en ApiExceptionHandler.
@Component
class HttpPaymentGateway implements PaymentGateway {

  private final RestClient client;

  HttpPaymentGateway(final RestClient.Builder builder, final PaymentProperties properties) {
    final SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(properties.timeout());
    requestFactory.setReadTimeout(properties.timeout());
    this.client =
        builder
            .baseUrl(properties.baseUrl())
            .requestFactory(requestFactory)
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.apiKey())
            .build();
  }

  @Override
  public void charge(final OrderId orderId, final Money amount) {
    try {
      client
          .post()
          .uri("/charges")
          .body(ChargeRequest.of(orderId, amount))
          .retrieve()
          .onStatus(
              HttpStatusCode::is4xxClientError,
              (request, response) -> {
                throw new PaymentDeclinedException(orderId);
              })
          .toBodilessEntity();
    } catch (final RestClientException exception) {
      throw new PaymentUnavailableException(orderId, exception);
    }
  }
}
