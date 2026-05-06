package com.proconsi.electrobazar.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity storing singleton-like company configuration (name, address, tax ID).
 */
@Entity
@Table(name = "company_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanySettings {

    /** Primary key (usually fixed to 1). */
    @Id
    @Column(name = "id")
    @Builder.Default
    private Long id = 1L;

    /** Name of the application/brand. */
    @Column(nullable = false)
    private String appName;

    /** Official company name. */
    @Column(nullable = false)
    private String name;

    /** Official tax identification number (NIF/CIF). */
    @Column(nullable = false)
    private String cif;

    /** Main business address. */
    @Column(nullable = false)
    private String address;

    /** City. */
    @Column(nullable = false)
    private String city;

    /** Postal code. */
    @Column(nullable = false)
    private String postalCode;

    /** Contact phone number. */
    @Column(nullable = false)
    private String phone;

    /** Contact email address. */
    @Column(nullable = false)
    private String email;

    /** Official website URL. */
    private String website;

    /** Legal registration details. */
    @Column(columnDefinition = "TEXT")
    private String registroMercantil;

    /** Default footer text for invoices and tickets. */
    @Column(columnDefinition = "TEXT")
    private String invoiceFooterText;

    /**
     * Number of days after the sale date during which a return is allowed.
     * This value is stamped on each ticket at creation time.
     * Default: 15 days.
     */
    @Column(name = "return_deadline_days")
    @Builder.Default
    private Integer returnDeadlineDays = 15;

    /**
     * Returns the invoice footer text with placeholders replaced.
     * Supported placeholders: {days} or {dias}
     */
    public String getFormattedFooterText(Integer days) {
        int targetDays = (days != null) ? days : (returnDeadlineDays != null ? returnDeadlineDays : 0);
        
        String baseText = (invoiceFooterText == null || invoiceFooterText.trim().isEmpty())
                ? "Gracias por su compra. Tiene {days} días para devoluciones."
                : invoiceFooterText;
        
        return baseText
                .replace("{days}", String.valueOf(targetDays))
                .replace("{dias}", String.valueOf(targetDays));
    }

    /** No-args version for templates that don't have a specific ticket context */
    public String getFormattedFooterText() {
        return getFormattedFooterText(returnDeadlineDays);
    }
}
