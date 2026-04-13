package com.proconsi.electrobazar.util;

import com.proconsi.electrobazar.model.Customer.IdDocumentType;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Utility component for validating identity and tax documents.
 *
 * <p>Supports per-type validation for all {@link IdDocumentType} values:</p>
 * <ul>
 *   <li>DNI  – 8 digits + letter (Spanish national ID)</li>
 *   <li>NIE  – X/Y/Z + 7 digits + letter (Spanish foreigner ID)</li>
 *   <li>NIF  – CIF algorithm letter + 7 digits + control (company tax ID)</li>
 *   <li>PASSPORT – ICAO format, 5–9 alphanumeric characters</li>
 *   <li>FOREIGN_ID – Lenient free-format, 4–25 chars</li>
 *   <li>INTRACOMMUNITY_VAT – 2-letter ISO country code + alphanumeric suffix</li>
 * </ul>
 *
 * <p>The legacy {@link #isValid(String)} method auto-detects among DNI/NIE/NIF
 * for backward-compatible CSV import processing.</p>
 */
@Component
public class NifCifValidator {

    // ── Patterns ────────────────────────────────────────────────────────────
    private static final Pattern DNI_PATTERN      = Pattern.compile("^\\d{8}[A-Z]$");
    private static final Pattern NIE_PATTERN      = Pattern.compile("^[XYZ]\\d{7}[A-Z]$");
    private static final Pattern NIF_CIF_PATTERN  = Pattern.compile("^[ABCDEFGHJNPQRSUVW]\\d{7}[0-9A-Z]$");
    private static final Pattern PASSPORT_PATTERN = Pattern.compile("^[A-Z0-9]{5,9}$");
    private static final Pattern FOREIGN_ID_PATTERN = Pattern.compile("^[A-Z0-9\\-]{4,25}$");
    private static final Pattern INTRACOM_PATTERN = Pattern.compile("^[A-Z]{2}[A-Z0-9]{2,12}$");

    private static final String DNI_LETTERS = "TRWAGMYFPDXBNJZSQVHLCKE";

    // ── Public API ───────────────────────────────────────────────────────────

    /**
     * Validates a document number for a specific document type.
     *
     * @param documentType the declared type of document
     * @param number       the document number string to validate
     * @return {@code true} if the number is blank (optional) or passes the type-specific check
     */
    public boolean isValidForType(IdDocumentType documentType, String number) {
        if (number == null || number.trim().isEmpty()) {
            return true; // optional – caller decides if blank is allowed
        }
        String n = number.trim().toUpperCase();

        if (documentType == null) {
            return isValid(n); // fallback: auto-detect
        }

        return switch (documentType) {
            case DNI               -> isDni(n);
            case NIE               -> isNie(n);
            case NIF               -> isNif(n);
            case PASSPORT          -> isPassport(n);
            case FOREIGN_ID        -> isForeignId(n);
            case INTRACOMMUNITY_VAT -> isIntracommunityVat(n);
        };
    }

    /**
     * Returns a human-readable validation error message, or {@code null} when valid.
     *
     * @param documentType the declared type of document
     * @param number       the document number to validate
     * @return error message string, or {@code null} if the value is acceptable
     */
    public String getValidationError(IdDocumentType documentType, String number) {
        if (number == null || number.trim().isEmpty()) return null;

        String n = number.trim().toUpperCase();
        if (documentType == null) {
            return isValid(n) ? null : "Formato de documento no reconocido";
        }

        return switch (documentType) {
            case DNI -> isDni(n) ? null
                    : "El DNI debe tener 8 dígitos seguidos de una letra (ej: 12345678Z)";
            case NIE -> isNie(n) ? null
                    : "El NIE debe comenzar con X, Y o Z, seguido de 7 dígitos y una letra (ej: X1234567L)";
            case NIF -> isNif(n) ? null
                    : "El CIF/NIF debe comenzar con una letra de empresa, 7 dígitos y un dígito/letra de control (ej: B12345678)";
            case PASSPORT -> isPassport(n) ? null
                    : "El número de pasaporte debe tener entre 5 y 9 caracteres alfanuméricos";
            case FOREIGN_ID -> isForeignId(n) ? null
                    : "El documento de identidad extranjero debe tener entre 4 y 25 caracteres";
            case INTRACOMMUNITY_VAT -> isIntracommunityVat(n) ? null
                    : "El NIF intracomunitario debe comenzar con 2 letras de país ISO (ej: DE123456789, FR12345678901)";
        };
    }

    /**
     * Legacy auto-detect validator: accepts DNI, NIE or NIF/CIF.
     * Returns {@code true} for blank values (field treated as optional by callers).
     *
     * @param taxId the tax ID string to validate
     * @return {@code true} if blank or matches a valid DNI / NIE / NIF
     */
    public boolean isValid(String taxId) {
        if (taxId == null || taxId.trim().isEmpty()) return true;
        String t = taxId.trim().toUpperCase();
        return isDni(t) || isNie(t) || isNif(t);
    }

    // ── Type-specific validators ─────────────────────────────────────────────

    /** DNI: 8 digits + control letter per AEAT modulo-23 table. */
    public boolean isDni(String s) {
        if (!DNI_PATTERN.matcher(s).matches()) return false;
        try {
            int number = Integer.parseInt(s.substring(0, 8));
            return DNI_LETTERS.charAt(number % 23) == s.charAt(8);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /** NIE: X/Y/Z prefix replaces with 0/1/2, then validates as DNI. */
    public boolean isNie(String s) {
        if (!NIE_PATTERN.matcher(s).matches()) return false;
        String prefix = switch (s.charAt(0)) {
            case 'X' -> "0";
            case 'Y' -> "1";
            case 'Z' -> "2";
            default  -> null;
        };
        return prefix != null && isDni(prefix + s.substring(1));
    }

    /** NIF / CIF: standard Spanish company tax ID (CIF algorithm). */
    public boolean isNif(String s) {
        if (!NIF_CIF_PATTERN.matcher(s).matches()) return false;
        return validateCifAlgorithm(s);
    }

    /** PASSPORT: ICAO numeric/alpha, 5–9 characters. */
    public boolean isPassport(String s) {
        return PASSPORT_PATTERN.matcher(s).matches();
    }

    /**
     * FOREIGN_ID: lenient check – 4 to 25 uppercase alphanumeric / hyphen chars.
     * No country-specific algorithm is applied.
     */
    public boolean isForeignId(String s) {
        return FOREIGN_ID_PATTERN.matcher(s).matches();
    }

    /**
     * INTRACOMMUNITY_VAT: ISO-3166-1 alpha-2 country code + 2–12 alphanumeric chars.
     * E.g. DE123456789, FR12345678901, IT12345678901.
     */
    public boolean isIntracommunityVat(String s) {
        return INTRACOM_PATTERN.matcher(s).matches();
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private boolean validateCifAlgorithm(String cif) {
        String digits  = cif.substring(1, 8);
        String control = cif.substring(8);

        int even = 0, odd = 0;
        for (int i = 0; i < digits.length(); i++) {
            int d = Character.getNumericValue(digits.charAt(i));
            if (i % 2 == 0) {
                int doubled = d * 2;
                odd += (doubled >= 10) ? (doubled - 9) : doubled;
            } else {
                even += d;
            }
        }

        int ctrl = (10 - ((even + odd) % 10)) % 10;
        char expectedLetter = "JABCDEFGHI".charAt(ctrl);
        char expectedDigit  = Character.forDigit(ctrl, 10);
        char actual = control.charAt(0);
        return actual == expectedDigit || actual == expectedLetter;
    }
}
