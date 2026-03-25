package com.proconsi.electrobazar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceMatrixSummaryDTO {
    private Long id;
    private Long productId;
    private String productName;
    private Long tariffId; // null for base product
    private String tariffName;
    private BigDecimal price;
    private BigDecimal oldPrice;
    private BigDecimal newPrice;
    private LocalDateTime startDate;
    private LocalDateTime createdAt;
    private boolean pending;
}
