package com.proconsi.electrobazar.service;

import com.proconsi.electrobazar.dto.TaxBreakdown;
import com.proconsi.electrobazar.util.RecargoEquivalenciaCalculator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for validating the precision and rounding order of SaleLine calculations.
 * 
 * Rules to verify:
 *  - baseAmount = quantity * unitPriceNet (rounded to 2dp HALF_UP)
 *  - vatAmount = baseAmount * vatRate (rounded to 2dp HALF_UP)
 *  - totalAmount = baseAmount + vatAmount (+ recargoAmount if applicable)
 */
@ExtendWith(MockitoExtension.class)
public class SaleLineCalculationTest {

    @InjectMocks
    private RecargoEquivalenciaCalculator calculator;

    @Test
    public void testFractionalQuantity21Vat() {
        // Case 1: 0.755 kg × 1.4239 €/kg (PVP) with IVA 21%
        BigDecimal qty = new BigDecimal("0.755");
        BigDecimal grossPrice = new BigDecimal("1.4239");
        BigDecimal vatRate = new BigDecimal("0.21");

        TaxBreakdown breakdown = calculator.calculateLineBreakdown(
                1L, "Producto Fraccionario 21%", 
                grossPrice, qty, vatRate, false);

        // Verification of 2 decimal places
        assertEquals(2, breakdown.getBaseAmount().scale());
        assertEquals(2, breakdown.getVatAmount().scale());
        assertEquals(2, breakdown.getTotalAmount().scale());

        // Calculation: 
        // netUnit = 1.4239 / 1.21 = 1.176776... 
        // base = 1.176776 * 0.755 = 0.8884... -> 0.89
        // vat = 0.89 * 0.21 = 0.1869 -> 0.19
        // total = 0.89 + 0.19 = 1.08
        assertEquals(new BigDecimal("0.89"), breakdown.getBaseAmount(), "Base Amount mismatch");
        assertEquals(new BigDecimal("0.19"), breakdown.getVatAmount(), "VAT Amount mismatch");
        assertEquals(new BigDecimal("1.08"), breakdown.getTotalAmount(), "Total Amount mismatch");
        
        // base + vat must equal total exactly
        assertEquals(breakdown.getBaseAmount().add(breakdown.getVatAmount()), breakdown.getTotalAmount());
    }

    @Test
    public void testFractionalQuantity10Vat() {
        // Case 2: 1.001 kg × 9.999 €/kg (PVP) with IVA 10%
        BigDecimal qty = new BigDecimal("1.001");
        BigDecimal grossPrice = new BigDecimal("9.999");
        BigDecimal vatRate = new BigDecimal("0.10");

        TaxBreakdown breakdown = calculator.calculateLineBreakdown(
                2L, "Producto Fraccionario 10%", 
                grossPrice, qty, vatRate, false);

        // Calculation:
        // netUnit = 9.999 / 1.10 = 9.09
        // base = 9.09 * 1.001 = 9.09909 -> 9.10
        // vat = 9.10 * 0.1 = 0.91
        // total = 9.10 + 0.91 = 10.01
        assertEquals(new BigDecimal("9.10"), breakdown.getBaseAmount(), "Base Amount mismatch");
        assertEquals(new BigDecimal("0.91"), breakdown.getVatAmount(), "VAT Amount mismatch");
        assertEquals(new BigDecimal("10.01"), breakdown.getTotalAmount(), "Total Amount mismatch");
    }

    @Test
    public void testStandardQuantity21Vat() {
        // Case 3: 3 ud × 5.00 € (PVP) with IVA 21%
        BigDecimal qty = new BigDecimal("3");
        BigDecimal grossPrice = new BigDecimal("5.00");
        BigDecimal vatRate = new BigDecimal("0.21");

        TaxBreakdown breakdown = calculator.calculateLineBreakdown(
                3L, "Producto Estándar 21%", 
                grossPrice, qty, vatRate, false);

        // Calculation:
        // netUnit = 5.00 / 1.21 = 4.132231...
        // base = 4.132231 * 3 = 12.39669... -> 12.40
        // vat = 12.40 * 0.21 = 2.604 -> 2.60
        // total = 12.40 + 2.60 = 15.00
        assertEquals(new BigDecimal("12.40"), breakdown.getBaseAmount());
        assertEquals(new BigDecimal("2.60"), breakdown.getVatAmount());
        assertEquals(new BigDecimal("15.00"), breakdown.getTotalAmount());
    }
}
