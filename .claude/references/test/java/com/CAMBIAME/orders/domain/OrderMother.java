package com.CAMBIAME.orders.domain;

import com.CAMBIAME.shared.domain.Money;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import org.instancio.Instancio;

// Object Mother: una factoría por escenario. Los agregados se construyen siempre con las
// factorías y métodos de negocio; los valores que los tests no comprueban (IDs, importes,
// cantidades, fechas) los genera Instancio dentro de rangos válidos.
// Las variantes que reciben OrderId son para los tests de integración, que piden el ID a
// orders.nextId() para que coincida con la secuencia.
public final class OrderMother {

  public static final Currency EUR = Currency.getInstance("EUR");

  private OrderMother() {}

  public static OrderId id() {
    return new OrderId(positiveLong());
  }

  public static CustomerId customerId() {
    return new CustomerId(positiveLong());
  }

  // Importe aleatorio válido: entre 0.01 y 1000.00, siempre en la misma divisa.
  public static Money price() {
    return new Money(BigDecimal.valueOf(Instancio.gen().ints().range(1, 100_000).get(), 2), EUR);
  }

  public static Order draft() {
    return draft(id(), customerId());
  }

  public static Order draft(final OrderId id, final CustomerId customerId) {
    return Order.place(id, customerId, EUR, Instancio.create(Instant.class));
  }

  public static Order draftWithOneLine() {
    return draftWithOneLine(id(), customerId());
  }

  public static Order draftWithOneLine(final OrderId id) {
    return draftWithOneLine(id, customerId());
  }

  public static Order draftWithOneLine(final OrderId id, final CustomerId customerId) {
    final Order order = draft(id, customerId);
    order.addLine(new ProductId(positiveLong()), price(), Instancio.gen().ints().range(1, 20).get());
    return order;
  }

  public static Order confirmed() {
    return confirmed(id());
  }

  public static Order confirmed(final OrderId id) {
    final Order order = draftWithOneLine(id);
    order.confirm();
    return order;
  }

  private static long positiveLong() {
    return Instancio.gen().longs().min(1L).get();
  }
}
