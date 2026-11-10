package com.CAMBIAME.orders.infrastructure.persistence;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

// Mapper de lectura: consultas planas para listados, sin pasar por el agregado.
// El orden siempre termina en el ID para que la paginación sea determinista.
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
