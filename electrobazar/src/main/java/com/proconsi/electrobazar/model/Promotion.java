package com.proconsi.electrobazar.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing an automatic promotion rule (NxM).
 * Promos are applied automatically to valid active dates and matching items.
 */
@Entity
@Table(name = "promotions")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Descriptive name of the promotion (e.g., "3x2 en Fundas de Móvil"). */
    @Column(nullable = false)
    private String name;

    /** Global activation switch. */
    @Column(nullable = false)
    private boolean active;

    /** The 'N' in NxM (Buy N items). */
    @Column(name = "n_value", nullable = false)
    private int nValue;

    /** The 'M' in NxM (Pay only for M items). */
    @Column(name = "m_value", nullable = false)
    private int mValue;

    /** Optional start date for the promotion. */
    private LocalDateTime validFrom;

    /** Optional end date for the promotion. */
    private LocalDateTime validUntil;

    /** Products that trigger this promotion. If empty, check categories. */
    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "promotion_products",
            joinColumns = @JoinColumn(name = "promotion_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id"))
    private Set<Product> restrictedProducts = new HashSet<>();

    /** Categories that trigger this promotion. If restrictedProducts is empty, all products in these categories apply. */
    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "promotion_categories",
            joinColumns = @JoinColumn(name = "promotion_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"))
    private Set<Category> restrictedCategories = new HashSet<>();

    /**
     * Checks if the promotion is currently active based on date and switch.
     */
    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        return active && (validFrom == null || now.isAfter(validFrom)) 
                      && (validUntil == null || now.isBefore(validUntil));
    }

    /**
     * Checks if a product is eligible for this promotion.
     */
    public boolean isApplicableTo(Product product) {
        if (product == null) return false;
        
        // If specific products are listed, the product must be in that list
        if (!restrictedProducts.isEmpty()) {
            return restrictedProducts.stream().anyMatch(p -> p.getId().equals(product.getId()));
        }
        
        // Otherwise, if categories are listed, the product's category must be in that list
        if (!restrictedCategories.isEmpty() && product.getCategory() != null) {
            return restrictedCategories.stream().anyMatch(c -> c.getId().equals(product.getCategory().getId()));
        }
        
        // If no restrictions are set, it applies to everything (rare but possible)
        return restrictedProducts.isEmpty() && restrictedCategories.isEmpty();
    }
}
