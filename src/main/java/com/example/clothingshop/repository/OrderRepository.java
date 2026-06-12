package com.example.clothingshop.repository;

import com.example.clothingshop.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    List<Order> findByUserIdOrderByIdDesc(Integer userId);

    Optional<Order> findByIdAndUserId(Integer id, Integer userId);

    @Query(value = "SELECT IFNULL(SUM(total_amount),0) FROM orders", nativeQuery = true)
    BigDecimal totalRevenue();

    @Query(value = "SELECT MONTH(created_at) as m, IFNULL(SUM(total_amount),0) as s FROM orders WHERE YEAR(created_at)=:yr GROUP BY MONTH(created_at) ORDER BY m", nativeQuery = true)
    List<Object[]> monthlyRevenue(@Param("yr") int year);

    @Query(value = "SELECT MONTH(created_at) as m, IFNULL(SUM(total_amount),0) as s FROM orders WHERE YEAR(created_at)=:yr AND status='COMPLETED' GROUP BY MONTH(created_at) ORDER BY m", nativeQuery = true)
    List<Object[]> monthlyCompletedRevenue(@Param("yr") int year);

    @Query(value = "SELECT MONTH(created_at) as m, COUNT(*) as c FROM orders WHERE YEAR(created_at)=:yr GROUP BY MONTH(created_at) ORDER BY m", nativeQuery = true)
    List<Object[]> monthlyOrderCount(@Param("yr") int year);

    @Query(value = "SELECT status, COUNT(*) as cnt, IFNULL(SUM(total_amount),0) as total FROM orders WHERE YEAR(created_at)=:yr GROUP BY status", nativeQuery = true)
    List<Object[]> orderStatusBreakdown(@Param("yr") int year);

    @Query(value = "SELECT oi.product_name, SUM(oi.quantity) as total_qty, SUM(oi.price * oi.quantity) as total_rev FROM order_items oi JOIN orders o ON oi.order_id = o.id WHERE YEAR(o.created_at)=:yr AND o.status='COMPLETED' GROUP BY oi.product_name ORDER BY total_rev DESC LIMIT 10", nativeQuery = true)
    List<Object[]> topProductsByRevenue(@Param("yr") int year);

    @Query(value = "SELECT IFNULL(SUM(total_amount),0) FROM orders WHERE YEAR(created_at)=:yr AND status='COMPLETED'", nativeQuery = true)
    BigDecimal completedRevenueByYear(@Param("yr") int year);

    @Query(value = "SELECT COUNT(*) FROM orders WHERE YEAR(created_at)=:yr", nativeQuery = true)
    long orderCountByYear(@Param("yr") int year);

    Page<Order> findAllByOrderByIdDesc(Pageable pageable);

    Page<Order> findByRecipientNameContainingIgnoreCaseOrderByIdDesc(String name, Pageable pageable);
}
