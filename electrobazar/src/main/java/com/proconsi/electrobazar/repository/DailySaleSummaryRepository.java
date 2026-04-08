package com.proconsi.electrobazar.repository;

import com.proconsi.electrobazar.model.DailySaleSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface DailySaleSummaryRepository extends JpaRepository<DailySaleSummary, LocalDate> {

    @Modifying
    @Query(value = """
            INSERT INTO daily_sales_stats (date, total_revenue, sales_count, cash_total, card_total, cancelled_count, cancelled_total)
            VALUES (:date, :amount, :count, :cash, :card, :cancelledCount, :cancelledAmount)
            ON DUPLICATE KEY UPDATE
                total_revenue = total_revenue + VALUES(total_revenue),
                sales_count = sales_count + VALUES(sales_count),
                cash_total = cash_total + VALUES(cash_total),
                card_total = card_total + VALUES(card_total),
                cancelled_count = cancelled_count + VALUES(cancelled_count),
                cancelled_total = cancelled_total + VALUES(cancelled_total)
            """, nativeQuery = true)
    void upsertDailyStats(
            @Param("date") LocalDate date,
            @Param("amount") BigDecimal amount,
            @Param("count") long count,
            @Param("cash") BigDecimal cash,
            @Param("card") BigDecimal card,
            @Param("cancelledCount") long cancelledCount,
            @Param("cancelledAmount") BigDecimal cancelledAmount
    );

    List<DailySaleSummary> findByDateBetweenOrderByDateAsc(LocalDate from, LocalDate to);
}
