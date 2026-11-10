package com.CAMBIAME.orders.infrastructure.web;

import static com.CAMBIAME.orders.domain.OrderMother.EUR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

import com.CAMBIAME.orders.application.AddOrderLineService;
import com.CAMBIAME.orders.application.ConfirmOrderService;
import com.CAMBIAME.orders.application.GetOrderService;
import com.CAMBIAME.orders.application.ListCustomerOrdersService;
import com.CAMBIAME.orders.application.OrderSummary;
import com.CAMBIAME.orders.application.PayOrderService;
import com.CAMBIAME.orders.application.PlaceOrderService;
import com.CAMBIAME.orders.domain.OrderId;
import com.CAMBIAME.orders.domain.OrderMother;
import com.CAMBIAME.orders.domain.OrderNotEditableException;
import com.CAMBIAME.orders.domain.OrderNotFoundException;
import com.CAMBIAME.orders.domain.OrderStatus;
import com.CAMBIAME.shared.domain.Money;
import java.math.BigDecimal;
import org.instancio.junit.InstancioExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

// Test de controlador: solo la traducción HTTP (rutas, códigos, validación, formato JSON). Los
// casos de uso van sustituidos con @MockitoBean. Uno por código HTTP distinto de cada endpoint;
// aquí se muestra uno de cada tipo. Literales solo en lo que se comprueba (el 42 de la ruta y del
// Location, el importe); el resto sale del Mother.
@WebMvcTest(OrderController.class)
@ExtendWith(InstancioExtension.class)
class OrderControllerTest {

  @Autowired private MockMvcTester mvc;

  @MockitoBean private PlaceOrderService placeOrder;
  @MockitoBean private AddOrderLineService addOrderLine;
  @MockitoBean private ConfirmOrderService confirmOrder;
  @MockitoBean private PayOrderService payOrder;
  @MockitoBean private GetOrderService getOrder;
  @MockitoBean private ListCustomerOrdersService listCustomerOrders;

  @Test
  void givenValidBody_whenPlaceOrder_thenCreatedWithLocation() {
    // given
    given(placeOrder.place(any())).willReturn(new OrderId(42));

    // when / then
    assertThat(
            mvc.post()
                .uri("/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"customerId": %d, "currency": "EUR"}
                    """.formatted(OrderMother.customerId().value())))
        .hasStatus(HttpStatus.CREATED)
        .hasHeader("Location", "/v1/orders/42");
  }

  @Test
  void givenMissingCustomerId_whenPlaceOrder_thenBadRequest() {
    // when / then
    assertThat(
            mvc.post()
                .uri("/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"currency": "EUR"}
                    """))
        .hasStatus(HttpStatus.BAD_REQUEST);
  }

  @Test
  void givenExistingOrder_whenGetOrder_thenMoneyIsADecimalString() {
    // given
    given(getOrder.get(any()))
        .willReturn(
            new OrderSummary(
                new OrderId(42),
                OrderMother.customerId(),
                OrderStatus.CONFIRMED,
                new Money(new BigDecimal("12.50"), EUR)));

    // when / then
    assertThat(mvc.get().uri("/v1/orders/42"))
        .hasStatus(HttpStatus.OK)
        .bodyJson()
        .extractingPath("$.total.amount")
        .isEqualTo("12.50");
  }

  @Test
  void givenUnknownOrder_whenGetOrder_thenNotFound() {
    // given
    given(getOrder.get(any())).willThrow(new OrderNotFoundException(new OrderId(42)));

    // when / then
    assertThat(mvc.get().uri("/v1/orders/42")).hasStatus(HttpStatus.NOT_FOUND);
  }

  @Test
  void givenNotEditableOrder_whenConfirm_thenConflict() {
    // given
    willThrow(new OrderNotEditableException(new OrderId(42))).given(confirmOrder).confirm(any());

    // when / then
    assertThat(mvc.post().uri("/v1/orders/42/confirmation")).hasStatus(HttpStatus.CONFLICT);
  }
}
