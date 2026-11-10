package com.CHANGEME.orders.infrastructure.persistence;

import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

// SQL in annotations with text blocks and parameters always with #{}. Works with *Row records;
// MyBatisOrderRepository does the conversion to the domain. Rows are built by column name
// (arg-name-based-constructor-auto-mapping in application.yml).
@Mapper
interface OrderMapper {

  @Select("SELECT nextval('orders_seq')")
  long nextId();

  @Select("""
      SELECT id, customer_id, currency, placed_at, discount_type, discount_value,
             status, total_amount, version
      FROM orders
      WHERE id = #{id}
      """)
  OrderRow findById(@Param("id") long id);

  @Select("""
      SELECT order_id, position, product_id, unit_price, quantity
      FROM order_lines
      WHERE order_id = #{orderId}
      ORDER BY position
      """)
  List<OrderLineRow> findLines(@Param("orderId") long orderId);

  @Insert("""
      INSERT INTO orders (id, customer_id, currency, placed_at, discount_type, discount_value,
                          status, total_amount, version)
      VALUES (#{id}, #{customerId}, #{currency}, #{placedAt}, #{discountType}, #{discountValue},
              #{status}, #{totalAmount}, 1)
      """)
  void insert(OrderRow row);

  @Update("""
      UPDATE orders
      SET discount_type = #{discountType},
          discount_value = #{discountValue},
          status = #{status},
          total_amount = #{totalAmount},
          version = version + 1
      WHERE id = #{id} AND version = #{version}
      """)
  int update(OrderRow row);

  @Delete("DELETE FROM order_lines WHERE order_id = #{orderId}")
  void deleteLines(@Param("orderId") long orderId);

  @Insert("""
      INSERT INTO order_lines (order_id, position, product_id, unit_price, quantity)
      VALUES (#{orderId}, #{position}, #{productId}, #{unitPrice}, #{quantity})
      """)
  void insertLine(OrderLineRow row);
}
