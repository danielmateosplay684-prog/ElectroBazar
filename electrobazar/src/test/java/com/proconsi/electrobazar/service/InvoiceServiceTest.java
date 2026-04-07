package com.proconsi.electrobazar.service;

import com.proconsi.electrobazar.model.Invoice;
import com.proconsi.electrobazar.model.Sale;
import com.proconsi.electrobazar.model.SaleLine;
import com.proconsi.electrobazar.repository.CompanySettingsRepository;
import com.proconsi.electrobazar.repository.InvoiceRepository;
import com.proconsi.electrobazar.repository.InvoiceSequenceRepository;
import com.proconsi.electrobazar.repository.SaleRepository;
import com.proconsi.electrobazar.service.impl.InvoiceServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.proconsi.electrobazar.model.InvoiceSequence;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Unit tests for InvoiceServiceImpl.
 *
 * Design rule enforced by these tests:
 *   - createInvoice() NEVER recalculates monetary amounts.
 *     It only assigns the sequential invoice number and the Verifactu hash chain.
 *   - generateRectificativeInvoice() ONLY negates already-rounded fields from
 *     the persisted SaleLines — it does NOT reapply percentages.
 *
 * All monetary fields in SaleLine are stored rounded to 2 decimal places in the DB,
 * so .negate() is sufficient to produce a fiscally symmetric mirror document.
 */
@ExtendWith(MockitoExtension.class)
public class InvoiceServiceTest {

    @Mock private InvoiceRepository invoiceRepository;
    @Mock private SaleRepository saleRepository;
    @Mock private InvoiceSequenceRepository invoiceSequenceRepository;
    @Mock private ActivityLogService activityLogService;
    @Mock private CompanySettingsRepository companySettingsRepository;

    @InjectMocks
    private InvoiceServiceImpl invoiceService;

    // -----------------------------------------------------------------------
    // Helper: builds a SaleLine with all monetary fields already rounded to 2dp
    // -----------------------------------------------------------------------
    private SaleLine buildLine(long id, String qty, String unit,
                                String baseAmt, String vatAmt, String vatRate,
                                String recargoAmt, String recargoRate, String subtotal) {
        return SaleLine.builder()
                .id(id)
                .quantity(new BigDecimal(qty))
                .unitPrice(new BigDecimal(unit))
                .baseAmount(new BigDecimal(baseAmt))
                .vatAmount(new BigDecimal(vatAmt))
                .vatRate(new BigDecimal(vatRate))
                .recargoAmount(new BigDecimal(recargoAmt))
                .recargoRate(new BigDecimal(recargoRate))
                .subtotal(new BigDecimal(subtotal))
                .build();
    }

    // -----------------------------------------------------------------------
    // TEST 1 – Single line with 21% VAT + RE (baseline symmetry check)
    // -----------------------------------------------------------------------
    @Test
    public void testGenerateRectificativeInvoiceFullSymmetry() {
        Sale originalSale = Sale.builder()
                .id(100L)
                .totalBase(new BigDecimal("100.00"))
                .totalVat(new BigDecimal("21.00"))
                .totalRecargo(new BigDecimal("5.20"))
                .totalAmount(new BigDecimal("126.20"))
                .totalDiscount(BigDecimal.ZERO)
                .invoice(Invoice.builder().invoiceNumber("F-2026-1").build())
                .build();

        SaleLine l1 = buildLine(1L, "1.000", "126.20",
                "100.00", "21.00", "0.2100",
                "5.20", "0.0520", "126.20");
        originalSale.setLines(List.of(l1));

        when(saleRepository.save(any(Sale.class))).thenAnswer(i -> i.getArguments()[0]);

        // Mocking for createInvoice()
        InvoiceSequence seq = InvoiceSequence.builder().serie("F").year(2026).lastNumber(0).build();
        when(invoiceSequenceRepository.findBySerieAndYearForUpdate(anyString(), anyInt())).thenReturn(Optional.of(seq));
        when(invoiceRepository.findByInvoiceNumber(anyString())).thenReturn(Optional.empty());
        when(invoiceRepository.findFirstBySerieOrderByYearDescSequenceNumberDesc(anyString())).thenReturn(Optional.empty());
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(i -> i.getArguments()[0]);

        Invoice rect = invoiceService.generateRectificativeInvoice(originalSale, "Devolución total");

        assertNotNull(rect);
        Sale neg = rect.getSale();

        // Header totals are exact negations
        assertEquals(originalSale.getTotalAmount().negate(),  neg.getTotalAmount(),  "totalAmount");
        assertEquals(originalSale.getTotalBase().negate(),    neg.getTotalBase(),    "totalBase");
        assertEquals(originalSale.getTotalVat().negate(),     neg.getTotalVat(),     "totalVat");
        assertEquals(originalSale.getTotalRecargo().negate(), neg.getTotalRecargo(), "totalRecargo");
        assertEquals(originalSale.getTotalDiscount().negate(),neg.getTotalDiscount(),"totalDiscount");

        // Line-level symmetry
        SaleLine negL = neg.getLines().get(0);
        assertEquals(l1.getQuantity().negate(),    negL.getQuantity());
        assertEquals(l1.getSubtotal().negate(),    negL.getSubtotal());
        assertEquals(l1.getBaseAmount().negate(),  negL.getBaseAmount());
        assertEquals(l1.getVatAmount().negate(),   negL.getVatAmount());
        assertEquals(l1.getRecargoAmount().negate(),negL.getRecargoAmount());

        // Pair must cancel to zero
        assertEquals(BigDecimal.ZERO.setScale(2),
                l1.getSubtotal().add(negL.getSubtotal()),
                "subtotal pair must be zero");
    }

    // -----------------------------------------------------------------------
    // TEST 2 – Multiple VAT types + RE: full desglose symmetry
    //
    // Scenario (all values pre-rounded to 2dp, as stored in DB):
    //   Line A: 2 uds × 50.00€ PVP  → 21% VAT, 5.2% RE
    //   Line B: 1.500 kg × 20.00€   → 21% VAT, no RE
    //   Line C: 3 uds × 10.00€      → 10% VAT, no RE
    //   Line D: 1 uds × 12.10€ PVP  → 10% VAT, 1.4% RE
    //
    // Pre-computed values (VAT-included price → extract base):
    //   A: base=82.64, vat=17.35, re=4.30, subtotal=100.00 (2×50.00)
    //   B: base=24.79, vat=5.21,  re=0.00, subtotal=30.00  (1.5×20.00)
    //   C: base=27.27, vat=2.73,  re=0.00, subtotal=30.00  (3×10.00)
    //   D: base=10.62, vat=1.06,  re=0.15, subtotal=12.10  (1×12.10)
    //   ─────────────────────────────────────────────────────────────
    //   Header: base=145.32, vat=26.35, re=4.45, total=172.10, discount=0
    // -----------------------------------------------------------------------
    @Test
    public void testRectificativeSymmetryMultipleVatTypes() {
        // --- Build original sale ---
        SaleLine la = buildLine(10L, "2.000", "50.00",
                "82.64", "17.35", "0.2100",
                "4.30",  "0.0520", "100.00");

        SaleLine lb = buildLine(11L, "1.500", "20.00",
                "24.79", "5.21", "0.2100",
                "0.00",  "0.0000", "30.00");

        SaleLine lc = buildLine(12L, "3.000", "10.00",
                "27.27", "2.73", "0.1000",
                "0.00",  "0.0000", "30.00");

        SaleLine ld = buildLine(13L, "1.000", "12.10",
                "10.62", "1.06", "0.1000",
                "0.15",  "0.0140", "12.10");

        Sale original = Sale.builder()
                .id(200L)
                .totalBase(new BigDecimal("145.32"))
                .totalVat(new BigDecimal("26.35"))
                .totalRecargo(new BigDecimal("4.45"))
                .totalAmount(new BigDecimal("172.10"))
                .totalDiscount(BigDecimal.ZERO)
                .invoice(Invoice.builder().invoiceNumber("F-2026-42").build())
                .build();
        original.setLines(new ArrayList<>(List.of(la, lb, lc, ld)));

        when(saleRepository.save(any(Sale.class))).thenAnswer(i -> i.getArguments()[0]);

        // Mocking for createInvoice()
        InvoiceSequence seq = InvoiceSequence.builder().serie("F").year(2026).lastNumber(41).build();
        when(invoiceSequenceRepository.findBySerieAndYearForUpdate(anyString(), anyInt())).thenReturn(Optional.of(seq));
        when(invoiceRepository.findByInvoiceNumber(anyString())).thenReturn(Optional.empty());
        when(invoiceRepository.findFirstBySerieOrderByYearDescSequenceNumberDesc(anyString())).thenReturn(Optional.empty());
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(i -> i.getArguments()[0]);

        // --- Generate rectificative ---
        Invoice rect = invoiceService.generateRectificativeInvoice(original, "Devolución múltiples tipos IVA");
        assertNotNull(rect);
        Sale neg = rect.getSale();

        // 1) Header totals sum to zero
        assertEquals(BigDecimal.ZERO.setScale(2),
                original.getTotalAmount().add(neg.getTotalAmount()),
                "Header totalAmount must cancel");
        assertEquals(BigDecimal.ZERO.setScale(2),
                original.getTotalBase().add(neg.getTotalBase()),
                "Header totalBase must cancel");
        assertEquals(BigDecimal.ZERO.setScale(2),
                original.getTotalVat().add(neg.getTotalVat()),
                "Header totalVat must cancel");
        assertEquals(BigDecimal.ZERO.setScale(2),
                original.getTotalRecargo().add(neg.getTotalRecargo()),
                "Header totalRecargo must cancel");

        // 2) Line-by-line: each pair of (original, rectificative) cancels to zero
        assertEquals(original.getLines().size(), neg.getLines().size(), "Line count must match");
        for (int i = 0; i < original.getLines().size(); i++) {
            SaleLine origLine = original.getLines().get(i);
            SaleLine negLine  = neg.getLines().get(i);

            assertEquals(BigDecimal.ZERO.setScale(2),
                    origLine.getSubtotal().add(negLine.getSubtotal()),
                    "subtotal pair [" + i + "] must cancel");
            assertEquals(BigDecimal.ZERO.setScale(2),
                    origLine.getBaseAmount().add(negLine.getBaseAmount()),
                    "baseAmount pair [" + i + "] must cancel");
            assertEquals(BigDecimal.ZERO.setScale(2),
                    origLine.getVatAmount().add(negLine.getVatAmount()),
                    "vatAmount pair [" + i + "] must cancel");
            assertEquals(BigDecimal.ZERO.setScale(2),
                    origLine.getRecargoAmount().add(negLine.getRecargoAmount()),
                    "recargoAmount pair [" + i + "] must cancel");
        }

        // 3) Grouped VAT desglose symmetry: for each vatRate, sum(base) and sum(vat) in
        //    orig + rect must both be zero — this is what the AEAT desglose shows.
        Map<BigDecimal, BigDecimal> origBaseByType = original.getLines().stream()
                .collect(Collectors.groupingBy(SaleLine::getVatRate,
                        Collectors.reducing(BigDecimal.ZERO, SaleLine::getBaseAmount, BigDecimal::add)));

        Map<BigDecimal, BigDecimal> negBaseByType = neg.getLines().stream()
                .collect(Collectors.groupingBy(SaleLine::getVatRate,
                        Collectors.reducing(BigDecimal.ZERO, SaleLine::getBaseAmount, BigDecimal::add)));

        for (BigDecimal rate : origBaseByType.keySet()) {
            BigDecimal origBase = origBaseByType.get(rate);
            BigDecimal negBase  = negBaseByType.getOrDefault(rate, BigDecimal.ZERO);
            assertEquals(BigDecimal.ZERO.setScale(2), origBase.add(negBase),
                    "Desglose base by vatRate " + rate + " must cancel");
        }

        Map<BigDecimal, BigDecimal> origVatByType = original.getLines().stream()
                .collect(Collectors.groupingBy(SaleLine::getVatRate,
                        Collectors.reducing(BigDecimal.ZERO, SaleLine::getVatAmount, BigDecimal::add)));

        Map<BigDecimal, BigDecimal> negVatByType = neg.getLines().stream()
                .collect(Collectors.groupingBy(SaleLine::getVatRate,
                        Collectors.reducing(BigDecimal.ZERO, SaleLine::getVatAmount, BigDecimal::add)));

        for (BigDecimal rate : origVatByType.keySet()) {
            BigDecimal origVat = origVatByType.get(rate);
            BigDecimal negVat  = negVatByType.getOrDefault(rate, BigDecimal.ZERO);
            assertEquals(BigDecimal.ZERO.setScale(2), origVat.add(negVat),
                    "Desglose VAT by rate " + rate + " must cancel");
        }

        // 4) totalRecargo sums to zero (explicit RE check)
        assertEquals(BigDecimal.ZERO.setScale(2),
                original.getTotalRecargo().add(neg.getTotalRecargo()),
                "totalRecargo must cancel to zero");
    }
}
