package com.proconsi.electrobazar.repository;

import com.proconsi.electrobazar.dto.SaleSummaryResponse;
import com.proconsi.electrobazar.dto.WorkerSaleStatsDTO;
import com.proconsi.electrobazar.model.PaymentMethod;
import com.proconsi.electrobazar.model.Sale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link Sale} entities.
 * Central hub for sales data, reporting, and dashboard statistics.
 */
@Repository
public interface SaleRepository extends JpaRepository<Sale, Long>, JpaSpecificationExecutor<Sale> {

    @EntityGraph(attributePaths = { "customer", "worker", "invoice", "ticket" })
    @Query("SELECT s FROM Sale s")
    Page<Sale> findAll(Pageable pageable);

    @EntityGraph(attributePaths = { "lines", "lines.product", "customer", "worker" })
    @Query("SELECT s FROM Sale s WHERE s.id = :id")
    Optional<Sale> findWithDetailsById(@Param("id") Long id);

    @EntityGraph(attributePaths = { "lines", "lines.product", "customer", "worker" })
    @Query("SELECT s FROM Sale s ORDER BY s.createdAt DESC")
    List<Sale> findAllWithDetails();

    @Override
    @EntityGraph(attributePaths = { "lines", "lines.product", "customer", "worker" })
    Optional<Sale> findById(Long id);

    @EntityGraph(attributePaths = { "lines", "lines.product", "customer", "worker" })
    List<Sale> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime from, LocalDateTime to);

    @EntityGraph(attributePaths = { "lines", "lines.product", "customer", "worker" })
    @Query("SELECT s FROM Sale s WHERE DATE(s.createdAt) = CURRENT_DATE ORDER BY s.createdAt DESC")
    List<Sale> findToday();

    @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM Sale s WHERE s.createdAt BETWEEN :from AND :to AND s.status = com.proconsi.electrobazar.model.Sale.SaleStatus.ACTIVE")
    BigDecimal sumTotalBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM Sale s WHERE s.createdAt BETWEEN :from AND :to AND s.paymentMethod = :method AND s.status = com.proconsi.electrobazar.model.Sale.SaleStatus.ACTIVE")
    Optional<BigDecimal> sumTotalBetweenByPaymentMethod(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to, @Param("method") PaymentMethod method);

    @Query("SELECT COUNT(s) FROM Sale s WHERE DATE(s.createdAt) = CURRENT_DATE AND s.status = com.proconsi.electrobazar.model.Sale.SaleStatus.ACTIVE")
    long countToday();

    @Query("SELECT COUNT(s) FROM Sale s WHERE s.createdAt BETWEEN :from AND :to AND s.status = com.proconsi.electrobazar.model.Sale.SaleStatus.ACTIVE")
    long countByCreatedAtBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT new com.proconsi.electrobazar.dto.SaleSummaryResponse(" +
            "COUNT(CASE WHEN s.status = com.proconsi.electrobazar.model.Sale.SaleStatus.ACTIVE THEN 1 END), " +
            "COALESCE(SUM(CASE WHEN s.status = com.proconsi.electrobazar.model.Sale.SaleStatus.ACTIVE THEN s.totalAmount ELSE 0 END), 0), " +
            "COALESCE(SUM(CASE WHEN s.status = com.proconsi.electrobazar.model.Sale.SaleStatus.ACTIVE AND s.paymentMethod = com.proconsi.electrobazar.model.PaymentMethod.CASH THEN s.totalAmount ELSE 0 END), 0), " +
            "COALESCE(SUM(CASE WHEN s.status = com.proconsi.electrobazar.model.Sale.SaleStatus.ACTIVE AND s.paymentMethod = com.proconsi.electrobazar.model.PaymentMethod.CARD THEN s.totalAmount ELSE 0 END), 0), " +
            "COUNT(CASE WHEN s.status = com.proconsi.electrobazar.model.Sale.SaleStatus.CANCELLED THEN 1 END), " +
            "COALESCE(SUM(CASE WHEN s.status = com.proconsi.electrobazar.model.Sale.SaleStatus.CANCELLED THEN s.totalAmount ELSE 0 END), 0)) " +
            "FROM Sale s WHERE s.createdAt BETWEEN :from AND :to")
    SaleSummaryResponse getSummaryBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query(value = "SELECT p.name_es FROM sales s JOIN sale_lines sl ON s.id = sl.sale_id JOIN products p ON sl.product_id = p.id WHERE s.created_at BETWEEN :from AND :to AND s.status = 'ACTIVE' GROUP BY p.name_es ORDER BY SUM(sl.quantity) DESC LIMIT 1", nativeQuery = true)
    String findTopProductNameBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @EntityGraph(attributePaths = { "lines", "lines.product", "worker" })
    @Query("SELECT s FROM Sale s WHERE s.customer.id = :customerId ORDER BY s.createdAt DESC")
    List<Sale> findByCustomerIdOrderByCreatedAtDesc(@Param("customerId") Long customerId);

    @Query("SELECT new com.proconsi.electrobazar.dto.WorkerSaleStatsDTO(w.id, w.username, COUNT(s), " +
           "SUM(s.totalAmount), " +
           "SUM(CASE WHEN s.paymentMethod = com.proconsi.electrobazar.model.PaymentMethod.CASH THEN s.totalAmount END), " +
           "SUM(CASE WHEN s.paymentMethod = com.proconsi.electrobazar.model.PaymentMethod.CARD THEN s.totalAmount END)) " +
           "FROM Sale s JOIN s.worker w " +
           "WHERE s.createdAt BETWEEN :from AND :to AND s.status = com.proconsi.electrobazar.model.Sale.SaleStatus.ACTIVE " +
           "GROUP BY w.id, w.username")
    List<WorkerSaleStatsDTO> getWorkerStatsBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /**
     * Aggregates daily revenue for charting.
     */
    @Query(value = "SELECT DATE(created_at) as date, SUM(total_amount) as total " +
           "FROM sales WHERE created_at BETWEEN :from AND :to AND status = 'ACTIVE' " +
           "GROUP BY DATE(created_at) ORDER BY date ASC", nativeQuery = true)
    List<Object[]> getDailyRevenue(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /**
     * Aggregates revenue by product category for the given range.
     */
    @Query("SELECT COALESCE(c.nameEs, 'Sin Categoría'), SUM(sl.subtotal) " +
           "FROM Sale s JOIN s.lines sl LEFT JOIN sl.product p LEFT JOIN p.category c " +
           "WHERE s.createdAt BETWEEN :from AND :to AND s.status = com.proconsi.electrobazar.model.Sale.SaleStatus.ACTIVE " +
           "GROUP BY c.nameEs")
    List<Object[]> getCategoryDistribution(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}