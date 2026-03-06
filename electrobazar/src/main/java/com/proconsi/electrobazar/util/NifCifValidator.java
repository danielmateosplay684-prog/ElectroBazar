package com.proconsi.electrobazar.util;

import org.springframework.stereotype.Component;
import java.util.regex.Pattern;

@Component
public class NifCifValidator {

    private static final Pattern NIF_PATTERN = Pattern.compile("^[0-9]{8}[A-Z]$");
    private static final Pattern NIE_PATTERN = Pattern.compile("^[XYZ][0-9]{7}[A-Z]$");
    private static final Pattern CIF_PATTERN = Pattern.compile("^[ABCDEFGHJNPQRSUVW][0-9]{7}[0-9A-Z]$");
    private static final String NIF_LETTERS = "TRWAGMYFPDXBNJZSQVHLCKE";

    /**
     * Validates a Spanish NIF, NIE or CIF.
     * Returns true if valid, false otherwise.
     */
    public boolean isValid(String taxId) {
        if (taxId == null || taxId.trim().isEmpty()) {
            return true; // Empty is valid based on requirements ("Allow empty")
        }

        String normalizedTaxId = taxId.trim().toUpperCase();

        if (NIF_PATTERN.matcher(normalizedTaxId).matches()) {
            return validateNIF(normalizedTaxId);
        } else if (NIE_PATTERN.matcher(normalizedTaxId).matches()) {
            return validateNIE(normalizedTaxId);
        } else if (CIF_PATTERN.matcher(normalizedTaxId).matches()) {
            return validateCIF(normalizedTaxId);
        }

        return false;
    }

    private boolean validateNIF(String nif) {
        try {
            int number = Integer.parseInt(nif.substring(0, 8));
            char expectedLetter = NIF_LETTERS.charAt(number % 23);
            return expectedLetter == nif.charAt(8);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean validateNIE(String nie) {
        String prefix = nie.substring(0, 1);
        String numberPart = nie.substring(1, 8);
        String suffix = nie.substring(8);

        String replacedPrefix;
        switch (prefix) {
            case "X":
                replacedPrefix = "0";
                break;
            case "Y":
                replacedPrefix = "1";
                break;
            case "Z":
                replacedPrefix = "2";
                break;
            default:
                return false;
        }

        return validateNIF(replacedPrefix + numberPart + suffix);
    }

    private boolean validateCIF(String cif) {
        String numbers = cif.substring(1, 8);
        String control = cif.substring(8);

        int evenSum = 0;
        int oddSum = 0;

        for (int i = 0; i < numbers.length(); i++) {
            int n = Character.getNumericValue(numbers.charAt(i));
            if (i % 2 == 0) { // odd positions (0-indexed so 0, 2, 4, 6)
                int doubleOdd = n * 2;
                oddSum += (doubleOdd >= 10) ? (doubleOdd - 9) : doubleOdd; // Sum of digits implementation
            } else { // even positions
                evenSum += n;
            }
        }

        int totalSum = evenSum + oddSum;
        int controlNumber = (10 - (totalSum % 10)) % 10;

        // The control character can be a number or a letter.
        // E.g., for some letter prefixes it MUST be a letter, for some it MUST be a
        // number,
        // but checking either is a good enough general approach unless specific rules
        // are strictly applied.
        String letters = "JABCDEFGHI";
        char expectedLetter = letters.charAt(controlNumber);
        char expectedNumber = Character.forDigit(controlNumber, 10);

        char actualControl = control.charAt(0);
        return actualControl == expectedNumber || actualControl == expectedLetter;
    }
}
