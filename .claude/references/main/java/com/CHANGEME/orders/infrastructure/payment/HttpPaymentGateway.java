package com.CHANGEME.orders.infrastructure.payment;

import com.CHANGEME.orders.application.PaymentDeclinedException;
import com.CHANGEME.orders.application.PaymentGateway;
import com.CHANGEME.orders.domain.OrderId;
import com.CHANGEME.shared.domain.Money;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

// Adapter of an output port to an external HTTP service, with RestClient.
// Needs spring-boot-starter-restclient: Spring Boot's RestClient.Builder propagates the
// traceId and records metrics. Base URL and timeouts come from PaymentProperties.
// Translates responses to the port's semantics: 4xx is a business rejection, anything else a
// technical failure. It does not log: the error is logged only once in ApiExceptionHandler.
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
