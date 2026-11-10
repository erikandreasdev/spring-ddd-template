package com.CHANGEME.orders.infrastructure.web;

import static com.CHANGEME.orders.domain.OrderMother.EUR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

import com.CHANGEME.orders.application.AddOrderLineService;
import com.CHANGEME.orders.application.ConfirmOrderService;
import com.CHANGEME.orders.application.GetOrderService;
import com.CHANGEME.orders.application.ListCustomerOrdersService;
import com.CHANGEME.orders.application.OrderSummary;
import com.CHANGEME.orders.application.PayOrderService;
import com.CHANGEME.orders.application.PlaceOrderService;
import com.CHANGEME.orders.domain.OrderId;
import com.CHANGEME.orders.domain.OrderMother;
import com.CHANGEME.orders.domain.OrderNotEditableException;
import com.CHANGEME.orders.domain.OrderNotFoundException;
import com.CHANGEME.orders.domain.OrderStatus;
import com.CHANGEME.shared.domain.Money;
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

// Controller test: only the HTTP translation (paths, status codes, validation, JSON format). Use
// cases are replaced with @MockitoBean. One per distinct HTTP status of each endpoint; here one
// of each kind is shown. Literals only for what is checked (the 42 in the path and in Location,
// the amount); the rest comes from the Mother.
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
