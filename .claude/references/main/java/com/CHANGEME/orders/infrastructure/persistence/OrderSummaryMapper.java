package com.CHANGEME.orders.infrastructure.persistence;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

// Read mapper: flat queries for listings, without going through the aggregate.
// Ordering always ends with the ID so that pagination is deterministic.
@Mapper
interface OrderSummaryMapper {

  @Select("""
      SELECT id, customer_id, status, total_amount, currency
      FROM orders
      WHERE customer_id = #{customerId}
      ORDER BY placed_at DESC, id DESC
      LIMIT #{size} OFFSET #{offset}
      """)
  List<OrderSummaryRow> findByCustomer(
      @Param("customerId") long customerId, @Param("size") int size, @Param("offset") long offset);

  @Select("SELECT COUNT(*) FROM orders WHERE customer_id = #{customerId}")
  long countByCustomer(@Param("customerId") long customerId);
}
