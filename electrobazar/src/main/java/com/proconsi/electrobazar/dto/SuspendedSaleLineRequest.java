package com.proconsi.electrobazar.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for a single cart line submitted when suspending a sale,
 * matching the JS cart structure: { productId, quantity, unitPrice, productName, vatRate }.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SuspendedSaleLineRequest {
    private Long productId;
    private String productName;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal vatRate;
}
