package com.proconsi.electrobazar.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Represents a pricing tariff (tarifa) that can be assigned to customers.
 *
 * <p>
 * System tariffs (MINORISTA, MAYORISTA, EMPLEADO) are seeded by
 * {@link com.proconsi.electrobazar.config.TariffDataInitializer} and cannot
 * be deleted. Custom tariffs can be created and deactivated.
 * </p>
 */
@Entity
@Table(name = "tariffs", indexes = {
        @Index(name = "idx_tariffs_name", columnList = "name", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tariff implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    /** System tariff names that cannot be deleted. */
    public static final String MINORISTA = "MINORISTA";
    public static final String MAYORISTA = "MAYORISTA";
    public static final String EMPLEADO = "EMPLEADO";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Short, uppercase name. E.g. MINORISTA, MAYORISTA, EMPLEADO. */
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    /**
     * Discount percentage applied to gross product price.
     * E.g. 15 means -15%. Must be 0–100.
     */
    @Column(nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal discountPercentage = BigDecimal.ZERO;

    @Column(length = 255)
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    /**
     * Whether this is a system‑defined tariff.
     * System tariffs (MINORISTA, MAYORISTA, EMPLEADO) cannot be deleted.
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean systemTariff = false;

    // ── Convenience ────────────────────────────────────────────────────────

    /**
     * Returns true if this tariff applies any discount (discountPercentage > 0).
     */
    public boolean hasDiscount() {
        return discountPercentage != null
                && discountPercentage.compareTo(BigDecimal.ZERO) > 0;
    }

    /** Human-readable label, e.g. "MAYORISTA -15%". */
    public String getDisplayLabel() {
        if (hasDiscount()) {
            return name + " -" + discountPercentage.stripTrailingZeros().toPlainString() + "%";
        }
        return name;
    }
}
