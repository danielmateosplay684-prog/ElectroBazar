package com.proconsi.electrobazar.repository;

import com.proconsi.electrobazar.model.DailyCategorySummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface DailyCategorySummaryRepository extends JpaRepository<DailyCategorySummary, Long> {

    @Modifying
    @Query(value = """
        INSERT INTO daily_category_stats (date, category_name, total_amount)
        VALUES (:date, :categoryName, :amount)
        ON DUPLICATE KEY UPDATE 
            total_amount = total_amount + VALUES(total_amount)
    """, nativeQuery = true)
    void upsertCategoryStat(@Param("date") LocalDate date, @Param("categoryName") String categoryName, @Param("amount") BigDecimal amount);

    /**
     * Gets total revenue per category for a date range from the summary table.
     */
    @Query("SELECT d.categoryName, SUM(d.totalAmount) as total FROM DailyCategorySummary d " +
           "WHERE d.date BETWEEN :from AND :to GROUP BY d.categoryName ORDER BY total DESC")
    List<Object[]> getDistributionBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
