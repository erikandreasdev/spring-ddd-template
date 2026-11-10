package com.CAMBIAME.orders;

import static org.assertj.core.api.Assertions.assertThat;

import com.CAMBIAME.IntegrationTest;
import com.CAMBIAME.orders.domain.OrderMother;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

// Flujo completo por HTTP, uno por agregado: comprueba que las piezas encajan (JSON real,
// código generado, casos de uso, transacciones, MyBatis y Flyway). No repite reglas de negocio.
@IntegrationTest
@AutoConfigureMockMvc
class OrderFlowTest {

  @Autowired private MockMvcTester mvc;

  @Test
  void givenNewOrder_whenAddingALineAndConfirming_thenOrderIsConfirmedWithItsTotal() {
    // given
    final MvcTestResult placed =
        mvc.post()
            .uri("/v1/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"customerId": %d, "currency": "EUR"}
                """.formatted(OrderMother.customerId().value()))
            .exchange();
    final String location = placed.getResponse().getHeader("Location");

    // when
    assertThat(
            mvc.post()
                .uri(location + "/lines")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"productId": %d, "unitPrice": {"amount": "12.50", "currency": "EUR"}, "quantity": 2}
                    """.formatted(Instancio.gen().longs().min(1L).get())))
        .hasStatus(HttpStatus.NO_CONTENT);
    assertThat(mvc.post().uri(location + "/confirmation")).hasStatus(HttpStatus.NO_CONTENT);

    // then
    assertThat(mvc.get().uri(location))
        .hasStatus(HttpStatus.OK)
        .bodyJson()
        .extractingPath("$.status")
        .isEqualTo("CONFIRMED");
    assertThat(mvc.get().uri(location))
        .bodyJson()
        .extractingPath("$.total.amount")
        .isEqualTo("25.00");
  }
}
