package com.CAMBIAME.orders.application;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.CAMBIAME.IntegrationTest;
import com.CAMBIAME.orders.domain.Order;
import com.CAMBIAME.orders.domain.OrderMother;
import com.CAMBIAME.orders.domain.OrderRepository;
import com.CAMBIAME.orders.domain.OrderStatus;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

// Caso de uso con un servicio externo: el adaptador real (HttpPaymentGateway) contra WireMock.
// Nunca @MockitoBean ni fakes sobre el puerto. El segundo test demuestra el rollback: el cobro
// rechazado deja el pedido como estaba.
@IntegrationTest
class PayOrderServiceTest {

  @Autowired private PayOrderService payOrder;
  @Autowired private OrderRepository orders;
  @Autowired private WireMockServer paymentProvider;

  // Preparación técnica: @BeforeEach nunca crea datos de negocio.
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
