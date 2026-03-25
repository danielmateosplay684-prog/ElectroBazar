package com.proconsi.electrobazar.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * One product line within a suspended sale.
 * Stores information needed to reconstruct the cart later.
 */
@Entity
@Table(name = "suspended_sale_lines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuspendedSaleLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The suspended sale this line belongs to. */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "suspended_sale_id", nullable = false)
    private SuspendedSale suspendedSale;

    /** The product in this line. Optional for wildcards. */
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "product_id", nullable = true, columnDefinition = "BIGINT NULL")
    private Product product;

    /** Manual name override for wildcard products. */
    @Column(length = 255)
    private String productName;

    /** Quantity of units in the cart. */
    @Column(nullable = false)
    private Integer quantity;

    /** Unit price (gross with VAT included) at suspension time. */
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    /** VAT rate at suspension time. */
    @Column(name = "vat_rate", precision = 5, scale = 4)
    private BigDecimal vatRate;
}


