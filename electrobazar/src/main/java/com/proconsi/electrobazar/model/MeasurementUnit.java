package com.proconsi.electrobazar.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing a measurement unit (e.g. Liter, Kg, Unit).
 */
@Entity
@Table(name = "measurement_units")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeasurementUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(nullable = false, length = 10)
    private String symbol;

    @Column(name = "decimal_places", nullable = false)
    @Builder.Default
    private int decimalPlaces = 0;

    @Column(name = "prompt_on_add", nullable = false)
    @Builder.Default
    private boolean promptOnAdd = false;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
