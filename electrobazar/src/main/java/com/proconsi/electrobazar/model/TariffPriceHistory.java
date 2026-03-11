package com.proconsi.electrobazar.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tariff_price_history", indexes = {
    @Index(name = "idx_tph_product", columnList = "product_id"),
    @Index(name = "idx_tph_tariff", columnList = "tariff_id"),
    @Index(name = "idx_tph_dates", columnList = "valid_from, valid_to")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TariffPriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tariff_id", nullable = false)
    private Tariff tariff;

    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "net_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal netPrice;

    @Column(name = "vat_rate", nullable = false, precision = 5, scale = 4)
    private BigDecimal vatRate;

    @Column(name = "price_with_vat", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceWithVat;

    @Column(name = "re_rate", nullable = false, precision = 5, scale = 4)
    private BigDecimal reRate;

    @Column(name = "price_with_re", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceWithRe;

    @Column(name = "discount_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal discountPercent;

    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    @Column(name = "valid_to")
    private LocalDate validTo;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    public void onPrePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
