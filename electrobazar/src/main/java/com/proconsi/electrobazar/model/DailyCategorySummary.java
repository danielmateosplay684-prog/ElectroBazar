package com.proconsi.electrobazar.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "daily_category_stats", uniqueConstraints = {
    @UniqueConstraint(name = "uk_date_category", columnNames = {"date", "categoryName"})
}, indexes = {
    @Index(name = "idx_daily_cat_date", columnList = "date")
})

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyCategorySummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private String categoryName;

    @Column(nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;
}
