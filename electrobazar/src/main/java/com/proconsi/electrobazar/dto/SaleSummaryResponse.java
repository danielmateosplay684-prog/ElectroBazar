package com.proconsi.electrobazar.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleSummaryResponse {
    private long totalSalesCount;
    private BigDecimal totalSalesAmount;
    private BigDecimal totalCashAmount;
    private BigDecimal totalCardAmount;
}
