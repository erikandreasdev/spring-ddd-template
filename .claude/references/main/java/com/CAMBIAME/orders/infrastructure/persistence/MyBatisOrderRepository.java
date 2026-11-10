package com.CAMBIAME.orders.infrastructure.persistence;

import com.CAMBIAME.orders.domain.CustomerId;
import com.CAMBIAME.orders.domain.Discount;
import com.CAMBIAME.orders.domain.Order;
import com.CAMBIAME.orders.domain.OrderId;
import com.CAMBIAME.orders.domain.OrderLine;
import com.CAMBIAME.orders.domain.OrderRepository;
import com.CAMBIAME.orders.domain.OrderSnapshot;
import com.CAMBIAME.orders.domain.OrderStatus;
import com.CAMBIAME.orders.domain.ProductId;
import com.CAMBIAME.shared.domain.ConcurrentUpdateException;
import com.CAMBIAME.shared.domain.Money;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

// Implementación del repositorio: toda la conversión entre filas y dominio vive aquí.
// Guarda por versión (bloqueo optimista) y las líneas se borran y reinsertan en cada save().
@Repository
class MyBatisOrderRepository implements OrderRepository {

  private final OrderMapper mapper;

  MyBatisOrderRepository(final OrderMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public OrderId nextId() {
    return new OrderId(mapper.nextId());
  }

  @Override
  public Optional<Order> findById(final OrderId id) {
    return Optional.ofNullable(mapper.findById(id.value()))
        .map(row -> Order.restore(toSnapshot(row, mapper.findLines(row.id()))));
  }

  @Override
  public void save(final Order order) {
    final OrderRow row = toRow(order);
    if (order.version() == 0) {
      mapper.insert(row);
    } else if (mapper.update(row) == 0) {
      throw new ConcurrentUpdateException("Order", order.id().value());
    }
    replaceLines(order);
  }

  private void replaceLines(final Order order) {
    final long orderId = order.id().value();
    mapper.deleteLines(orderId);
    final List<OrderLine> lines = order.lines();
    for (int position = 0; position < lines.size(); position++) {
      final OrderLine line = lines.get(position);
      mapper.insertLine(
          new OrderLineRow(
              orderId,
              position,
              line.productId().value(),
              line.unitPrice().amount(),
              line.quantity()));
    }
  }

  private static OrderRow toRow(final Order order) {
    final OrderSnapshot snapshot = order.snapshot();
    final DiscountColumns discount = toColumns(snapshot.discount());
    return new OrderRow(
        snapshot.id().value(),
        snapshot.customerId().value(),
        snapshot.currency().getCurrencyCode(),
        snapshot.placedAt(),
        discount.type(),
        discount.value(),
        snapshot.status().name(),
        order.total().amount(),
        snapshot.version());
  }

  private static OrderSnapshot toSnapshot(final OrderRow row, final List<OrderLineRow> lines) {
    final Currency currency = Currency.getInstance(row.currency());
    return new OrderSnapshot(
        new OrderId(row.id()),
        new CustomerId(row.customerId()),
        currency,
        row.placedAt(),
        lines.stream().map(line -> toLine(line, currency)).toList(),
        toDiscount(row.discountType(), row.discountValue(), currency),
        OrderStatus.valueOf(row.status()),
        row.version());
  }

  private static OrderLine toLine(final OrderLineRow row, final Currency currency) {
    return new OrderLine(
        new ProductId(row.productId()), new Money(row.unitPrice(), currency), row.quantity());
  }

  // Switch exhaustivo sobre la interfaz sellada, sin default: si se añade una variante, deja de
  // compilar hasta que se trate aquí.
  private static DiscountColumns toColumns(final Discount discount) {
    return switch (discount) {
      case Discount.NoDiscount none -> new DiscountColumns("NONE", BigDecimal.ZERO);
      case Discount.PercentageDiscount percentage ->
          new DiscountColumns("PERCENTAGE", BigDecimal.valueOf(percentage.percent()));
      case Discount.FixedDiscount fixed -> new DiscountColumns("FIXED", fixed.value().amount());
    };
  }

  // Aquí el switch es sobre un String de la base de datos, así que necesita default.
  private static Discount toDiscount(
      final String type, final BigDecimal value, final Currency currency) {
    return switch (type) {
      case "NONE" -> new Discount.NoDiscount();
      case "PERCENTAGE" -> new Discount.PercentageDiscount(value.intValueExact());
      case "FIXED" -> new Discount.FixedDiscount(new Money(value, currency));
      default -> throw new IllegalStateException("Unknown discount type: " + type);
    };
  }

  private record DiscountColumns(String type, BigDecimal value) {}
}
