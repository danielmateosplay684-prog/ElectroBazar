package com.proconsi.electrobazar.util;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class VerifactuHashCalculatorTest {

    private final VerifactuHashCalculator calculator = new VerifactuHashCalculator();

    @Test
    public void testHashCalculationAeatSpecs() {
        // Input from AEAT technical documentation example
        String nif = "89890001K";
        String numSerie = "12345678/G33";
        LocalDateTime fechaHora = LocalDateTime.of(2024, 1, 1, 19, 20, 30);
        String tipoFactura = "F1";
        BigDecimal cuotaTotal = new BigDecimal("12.35");
        BigDecimal importeTotal = new BigDecimal("123.45");
        String huellaAnterior = ""; // Empty for the first invoice

        String expectedHash = "3C464DAF61ACB827C65FDA19F352A4E3BDC2C640E9E9FC4CC058073F38F12F60";

        String actualHash = calculator.calculate(
                nif, numSerie, fechaHora, tipoFactura,
                cuotaTotal, importeTotal, huellaAnterior
        );

        assertEquals(expectedHash, actualHash, "Hash must match AEAT technical specification example");
    }
}
