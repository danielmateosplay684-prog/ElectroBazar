package com.proconsi.electrobazar.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Entity representing a single line item in a sale.
 * Tracks pricing, taxes, and discounts at the time of the transaction.
 */
@Entity
@Table(name = "sale_lines", indexes = {
        @Index(name = "idx_sale_lines_sale_id", columnList = "sale_id"),
        @Index(name = "idx_sale_lines_product_id", columnList = "product_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The sale this line belongs to. */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale;

    /** The product sold. Optional for wildcard products. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = true, columnDefinition = "BIGINT NULL")
    private Product product;

    /** Custom name for the product (mandatory for wildcard products). */
    @Column(length = 255)
    private String productName;

    /** Number of decimals for quantity. */
    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantity;

    /**
     * Gross unit price AFTER applying the tariff discount.
     * What the customer pays per unit (VAT included).
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    /**
     * Gross unit price BEFORE applying any tariff discount (catalogue price).
     */
    @Column(nullable = false, precision = 10, scale = 2, name = "original_unit_price")
    @Builder.Default
    private BigDecimal originalUnitPrice = BigDecimal.ZERO;

    /**
     * Discount percentage applied specifically to this line.
     */
    @Column(nullable = false, precision = 5, scale = 2, name = "discount_percentage")
    @Builder.Default
    private BigDecimal discountPercentage = BigDecimal.ZERO;

    /** Net unit base price (before tax). */
    @Column(nullable = false, precision = 10, scale = 2, name = "base_price_net")
    @Builder.Default
    private BigDecimal basePriceNet = BigDecimal.ZERO;

    /** VAT rate at the time of sale. */
    @Column(nullable = false, precision = 5, scale = 4, name = "vat_rate")
    @Builder.Default
    private BigDecimal vatRate = BigDecimal.ZERO;

    /** Gross subtotal for this line (quantity * unitPrice). */
    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    /** Total taxable base for this line. */
    @Column(nullable = false, precision = 10, scale = 2, name = "base_amount")
    @Builder.Default
    private BigDecimal baseAmount = BigDecimal.ZERO;

    /** Total VAT amount for this line. */
    @Column(nullable = false, precision = 10, scale = 2, name = "vat_amount")
    @Builder.Default
    private BigDecimal vatAmount = BigDecimal.ZERO;

    /** Recargo de Equivalencia rate applied to this line. */
    @Column(nullable = false, precision = 5, scale = 4, name = "recargo_rate")
    @Builder.Default
    private BigDecimal recargoRate = BigDecimal.ZERO;

    /** Total RE amount for this line. */
    @Column(nullable = false, precision = 10, scale = 2, name = "recargo_amount")
    @Builder.Default
    private BigDecimal recargoAmount = BigDecimal.ZERO;
}