package com.CHANGEME.orders.domain;

import com.CHANGEME.shared.domain.Money;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

// Aggregate root. Private constructor, creation factory with a business name and restore() for
// persistence. No setters: state only changes through business methods that protect the
// invariants. The time arrives as a parameter; the use case takes it from a Clock.
public final class Order {

  private final OrderId id;
  private final CustomerId customerId;
  private final Currency currency;
  private final Instant placedAt;
  private final List<OrderLine> lines;
  private final long version;
  private Discount discount;
  private OrderStatus status;

  private Order(final OrderSnapshot snapshot) {
    this.id = snapshot.id();
    this.customerId = snapshot.customerId();
    this.currency = snapshot.currency();
    this.placedAt = snapshot.placedAt();
    this.lines = new ArrayList<>(snapshot.lines());
    this.discount = snapshot.discount();
    this.status = snapshot.status();
    this.version = snapshot.version();
  }

  public static Order place(
      final OrderId id,
      final CustomerId customerId,
      final Currency currency,
      final Instant placedAt) {
    return new Order(
        new OrderSnapshot(
            id,
            customerId,
            currency,
            placedAt,
            List.of(),
            new Discount.NoDiscount(),
            OrderStatus.DRAFT,
            0));
  }

  public static Order restore(final OrderSnapshot snapshot) {
    return new Order(snapshot);
  }

  public void addLine(final ProductId productId, final Money unitPrice, final int quantity) {
    requireEditable();
    if (!unitPrice.currency().equals(currency)) {
      throw new IllegalArgumentException(
          "Order " + id.value() + " uses " + currency + ", not " + unitPrice.currency());
    }
    lines.add(new OrderLine(productId, unitPrice, quantity));
  }

  public void applyDiscount(final Discount newDiscount) {
    requireEditable();
    discount = newDiscount;
  }

  public void confirm() {
    if (lines.isEmpty()) throw new EmptyOrderException(id);
    requireEditable();
    status = OrderStatus.CONFIRMED;
  }

  public void markPaid() {
    if (status != OrderStatus.CONFIRMED) throw new OrderNotConfirmedException(id);
    status = OrderStatus.PAID;
  }

  public Money subtotal() {
    return lines.stream().map(OrderLine::subtotal).reduce(Money.zero(currency), Money::plus);
  }

  public Money total() {
    return discount.applyTo(subtotal());
  }

  public OrderSnapshot snapshot() {
    return new OrderSnapshot(
        id, customerId, currency, placedAt, lines, discount, status, version);
  }

  public OrderId id() {
    return id;
  }

  public CustomerId customerId() {
    return customerId;
  }

  public OrderStatus status() {
    return status;
  }

  public List<OrderLine> lines() {
    return List.copyOf(lines);
  }

  public long version() {
    return version;
  }

  public boolean isEditable() {
    return status == OrderStatus.DRAFT;
  }

  private void requireEditable() {
    if (!isEditable()) throw new OrderNotEditableException(id);
  }
}
