package com.proconsi.electrobazar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO returning applied automatic promotion details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionCalcResponse {
    private BigDecimal totalDiscount;
    private List<String> appliedPromotions;
}
