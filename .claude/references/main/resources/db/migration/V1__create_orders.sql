-- One sequence per aggregate table, named constraints, NOT NULL by default,
-- money as DECIMAL(19, 4) plus the currency, enums by name and time-zone-aware dates.

CREATE SEQUENCE orders_seq;

CREATE TABLE orders (
  id             BIGINT         NOT NULL,
  customer_id    BIGINT         NOT NULL,
  currency       CHAR(3)        NOT NULL,
  placed_at      TIMESTAMPTZ    NOT NULL,
  discount_type  VARCHAR(20)    NOT NULL,
  discount_value DECIMAL(19, 4) NOT NULL,
  status         VARCHAR(20)    NOT NULL,
  total_amount   DECIMAL(19, 4) NOT NULL,
  version        BIGINT         NOT NULL,
  CONSTRAINT pk_orders PRIMARY KEY (id)
);

CREATE INDEX ix_orders_customer_id ON orders (customer_id, placed_at DESC, id DESC);

-- Aggregate collection: no ID of its own, composite key of order and position.
CREATE TABLE order_lines (
  order_id   BIGINT         NOT NULL,
  position   INT            NOT NULL,
  product_id BIGINT         NOT NULL,
  unit_price DECIMAL(19, 4) NOT NULL,
  quantity   INT            NOT NULL,
  CONSTRAINT pk_order_lines PRIMARY KEY (order_id, position),
  CONSTRAINT fk_order_lines_orders FOREIGN KEY (order_id) REFERENCES orders (id)
);
