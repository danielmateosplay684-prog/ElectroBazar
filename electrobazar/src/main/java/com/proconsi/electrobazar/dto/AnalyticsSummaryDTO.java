package com.proconsi.electrobazar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Data Transfer Object for pre-calculated analytics statistics.
 * Avoids the need to transfer full sale records to the frontend for charting.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsSummaryDTO {
    private long totalSales;
    private BigDecimal totalRevenue;
    private BigDecimal cashRevenue;
    private BigDecimal cardRevenue;
    private long cancelledSales;
    private BigDecimal cancelledRevenue;
    private String topProductName;
    private long lowStockCount;
    
    /**
     * Data points for charts.
     * Key: ISO date string or hour (e.g., "2026-03-26" or "14:00")
     * Value: Revenue for that point.
     */
    private Map<String, BigDecimal> revenueTrend;

    /**
     * Data points for category distribution chart.
     * Key: Category name.
     * Value: Total subtotal for that category.
     */
    private Map<String, BigDecimal> categoryDistribution;
}
