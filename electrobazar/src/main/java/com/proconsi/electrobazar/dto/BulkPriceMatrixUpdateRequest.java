package com.proconsi.electrobazar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkPriceMatrixUpdateRequest {
    private LocalDateTime effectiveDate;
    private List<PriceChangeItem> changes;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriceChangeItem {
        private Long productId;
        private Long tariffId; // null for base product price
        private BigDecimal newPrice;
    }
}
