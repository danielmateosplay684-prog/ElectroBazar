package com.proconsi.electrobazar.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "company_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanySettings {

    @Id
    @Column(name = "id")
    private Long id = 1L;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String cif;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String postalCode;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String email;

    private String website;

    @Column(columnDefinition = "TEXT")
    private String registroMercantil;

    @Column(columnDefinition = "TEXT")
    private String invoiceFooterText;
}
