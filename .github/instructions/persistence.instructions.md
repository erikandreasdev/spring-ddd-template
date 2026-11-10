---
applyTo: "src/main/java/**/infrastructure/persistence/**/*.java,src/main/resources/db/**"
---

# Persistence

## Mappers and repositories
- Mappers with SQL in annotations and text blocks. Parameters always with `#{}`, never `${}`.
- Conversion between `*Row` and domain happens in the repository implementation, without a
  `TypeHandler` for typed IDs.
- `nextId()` reads the table's sequence. In PostgreSQL: `SELECT nextval('orders_seq')`.
  <!-- CHANGEME: database. Sequence syntax in your engine -->
- `save(...)` decides by version: if it is 0, `INSERT` with version 1; otherwise,
  `UPDATE ... SET ..., version = version + 1 WHERE id = #{id} AND version = #{version}`.
  If the `UPDATE` affects 0 rows, it throws `ConcurrentUpdateException`.
- The aggregate's collections are saved by deleting and reinserting their rows in the same
  `save(...)`.

## Schema
- Changes only with Flyway: `src/main/resources/db/migration/V<n>__<description>.sql`. A
  migration that has already been applied is never modified; add a new one.
- Tables in plural and `snake_case`: `orders`, `order_lines`.
- Every aggregate table has its sequence (`orders_seq`), primary key `id BIGINT` and a
  `version BIGINT NOT NULL` column.
- Collection tables use a composite key: aggregate ID plus `position`.
- Constraints with explicit names: `pk_orders`, `fk_order_lines_orders`,
  `uk_customers_email`. An index on every foreign key.
- `NOT NULL` by default.
- Money: `DECIMAL(19, 4)` plus the currency in `CHAR(3)`.
- Enums stored by name, never by position.
- Dates in a time-zone-aware column, in UTC.
- Physical deletes. No `deleted` columns unless the business needs history.
