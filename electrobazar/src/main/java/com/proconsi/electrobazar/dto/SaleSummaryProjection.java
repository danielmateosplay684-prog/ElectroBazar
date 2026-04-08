package com.proconsi.electrobazar.dto;

import java.math.BigDecimal;

/**
 * Interface projection for aggregated sales statistics.
 * Used to map native SQL results efficiently without Hibernate constructor issues.
 */
public interface SaleSummaryProjection {
    
    Long getTotalSalesCount();
    
    BigDecimal getTotalSalesAmount();
    
    BigDecimal getTotalCashAmount();
    
    BigDecimal getTotalCardAmount();
    
    Long getTotalCancelledCount();
    
    BigDecimal getTotalCancelledAmount();
}
