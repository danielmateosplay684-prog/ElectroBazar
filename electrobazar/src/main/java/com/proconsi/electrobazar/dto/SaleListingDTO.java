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
public class SaleListingDTO {
    private Long id;
    private LocalDateTime createdAt;
    private String customerName;
    private String workerUsername;
    private String paymentMethod;
    private BigDecimal totalAmount;
    private String status;
    private String invoiceNumber;
    private String ticketNumber;
}
