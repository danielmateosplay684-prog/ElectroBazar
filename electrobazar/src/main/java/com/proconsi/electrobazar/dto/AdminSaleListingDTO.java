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
public class AdminSaleListingDTO {
    private Long id;
    private String displayId;
    private LocalDateTime createdAt;
    private String type; // "factura", "ticket"
    private String status;
    private String customerName;
    private String customerTaxId;
    private String workerUsername;
    private String paymentMethod;
    private BigDecimal totalAmount;
}
