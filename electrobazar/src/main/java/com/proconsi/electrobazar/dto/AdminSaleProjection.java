package com.proconsi.electrobazar.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Interface-based projection for high-performance native SQL sale listings.
 */
public interface AdminSaleProjection {
    Long getId();
    LocalDateTime getCreatedAt();
    BigDecimal getTotalAmount();
    String getPaymentMethod();
    String getStatus();
    String getCustomerName();
    String getCustomerTaxId();
    String getWorkerUsername();
    String getDisplayId();
    String getType();
}
