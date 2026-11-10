package com.CHANGEME.orders.application;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.CHANGEME.IntegrationTest;
import com.CHANGEME.orders.domain.Order;
import com.CHANGEME.orders.domain.OrderMother;
import com.CHANGEME.orders.domain.OrderRepository;
import com.CHANGEME.orders.domain.OrderStatus;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

// Use case with an external service: the real adapter (HttpPaymentGateway) against WireMock.
// Never @MockitoBean or fakes on the port. The second test proves the rollback: the declined
// charge leaves the order as it was.
@IntegrationTest
class PayOrderServiceTest {

  @Autowired private PayOrderService payOrder;
  @Autowired private OrderRepository orders;
  @Autowired private WireMockServer paymentProvider;

  // Technical setup: @BeforeEach never creates business data.
  @BeforeEach
  void resetPaymentProvider() {
    paymentProvider.resetAll();
  }

  @Test
  void givenConfirmedOrder_whenPay_thenOrderIsPaidAndCharged() {
    // given
    final Order order = OrderMother.confirmed(orders.nextId());
    orders.save(order);
    paymentProvider.stubFor(post(urlEqualTo("/charges")).willReturn(aResponse().withStatus(201)));

    // when
    payOrder.pay(new PayOrderCommand(order.id()));

    // then
    assertThat(orders.findById(order.id()).orElseThrow().status()).isEqualTo(OrderStatus.PAID);
    paymentProvider.verify(
        postRequestedFor(urlEqualTo("/charges"))
            .withRequestBody(matchingJsonPath("$.reference", equalTo("order-" + order.id().value())))
            .withRequestBody(
                matchingJsonPath("$.amount", equalTo(order.total().amount().toPlainString()))));
  }

  @Test
  void givenDeclinedPayment_whenPay_thenOrderStaysConfirmed() {
    // given
    final Order order = OrderMother.confirmed(orders.nextId());
    orders.save(order);
    paymentProvider.stubFor(post(urlEqualTo("/charges")).willReturn(aResponse().withStatus(402)));

    // when / then
    assertThatThrownBy(() -> payOrder.pay(new PayOrderCommand(order.id())))
        .isInstanceOf(PaymentDeclinedException.class);
    assertThat(orders.findById(order.id()).orElseThrow().status())
        .isEqualTo(OrderStatus.CONFIRMED);
  }
}
