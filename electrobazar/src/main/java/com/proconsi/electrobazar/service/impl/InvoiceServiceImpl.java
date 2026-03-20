package com.proconsi.electrobazar.service.impl;

import com.proconsi.electrobazar.model.Invoice;
import com.proconsi.electrobazar.model.InvoiceSequence;
import com.proconsi.electrobazar.model.Sale;
import com.proconsi.electrobazar.model.SaleLine;
import com.proconsi.electrobazar.repository.InvoiceRepository;
import com.proconsi.electrobazar.repository.InvoiceSequenceRepository;
import com.proconsi.electrobazar.repository.SaleRepository;
import com.proconsi.electrobazar.service.ActivityLogService;
import com.proconsi.electrobazar.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of {@link InvoiceService}.
 * Manages the generation of legal invoices with strict sequential numbering.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceServiceImpl implements InvoiceService {

    private static final String DEFAULT_SERIE = "F";

    private final InvoiceRepository invoiceRepository;
    private final InvoiceSequenceRepository invoiceSequenceRepository;
    private final SaleRepository saleRepository;
    private final ActivityLogService activityLogService;

    /**
     * Atomically increments the invoice sequence for the current year and serie.
     * Uses PESSIMISTIC_WRITE locking to prevent race conditions and duplicate numbering.
     *
     * @param sale The sale entity to link with the invoice.
     * @return The newly generated Invoice.
     */
    @Override
    @Transactional
    public Invoice createInvoice(Sale sale) {
        int invoiceYear = sale.getCreatedAt() != null ? sale.getCreatedAt().getYear() : LocalDate.now().getYear();
        String serie = DEFAULT_SERIE;

        // Fetch and lock the sequence row.
        InvoiceSequence sequence = invoiceSequenceRepository
                .findBySerieAndYearForUpdate(serie, invoiceYear)
                .orElseGet(() -> {
                    InvoiceSequence newSeq = InvoiceSequence.builder()
                            .serie(serie)
                            .year(invoiceYear)
                            .lastNumber(0)
                            .build();
                    return invoiceSequenceRepository.save(newSeq);
                });

        String invoiceNumber;
        do {
            int nextNumber = sequence.getLastNumber() + 1;
            sequence.setLastNumber(nextNumber);
            invoiceSequenceRepository.save(sequence);

            // Format example: F-2026-1
            invoiceNumber = String.format("%s-%d-%d", serie, invoiceYear, nextNumber);
        } while (invoiceRepository.findByInvoiceNumber(invoiceNumber).isPresent());

        Invoice invoice = Invoice.builder()
                .invoiceNumber(invoiceNumber)
                .serie(serie)
                .year(invoiceYear)
                .sequenceNumber(sequence.getLastNumber())
                .sale(sale)
                .status(Invoice.InvoiceStatus.ACTIVE)
                .build();

        Invoice saved = invoiceRepository.save(invoice);
        log.info("Invoice generated: {} for Sale ID {}", invoiceNumber, sale.getId());

        activityLogService.logActivity(
                "CREAR_FACTURA",
                String.format("Invoice %s generated for Sale #%d", invoiceNumber, sale.getId()),
                "System",
                "INVOICE",
                saved.getId());

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Invoice> findBySaleId(Long saleId) {
        return invoiceRepository.findBySaleId(saleId);
    }

    @Override
    @Transactional
    public Invoice generateRectificativeInvoice(Sale originalSale, String reason) {
        Invoice originalInvoice = originalSale.getInvoice();
        if (originalInvoice == null) return null;

        Sale negativeSale = Sale.builder()
                .paymentMethod(originalSale.getPaymentMethod())
                .customer(originalSale.getCustomer())
                .worker(originalSale.getWorker())
                .applyRecargo(originalSale.isApplyRecargo())
                .appliedTariff(originalSale.getAppliedTariff())
                .totalAmount(originalSale.getTotalAmount().negate())
                .totalBase(originalSale.getTotalBase().negate())
                .totalVat(originalSale.getTotalVat().negate())
                .totalRecargo(originalSale.getTotalRecargo().negate())
                .notes("RECTIFICATIVA de " + originalInvoice.getInvoiceNumber() + ". Motivo: " + reason)
                .status(Sale.SaleStatus.CANCELLED)
                .build();

        List<SaleLine> negLines = originalSale.getLines().stream().map(l -> SaleLine.builder()
                .product(l.getProduct()).quantity(-l.getQuantity()).unitPrice(l.getUnitPrice())
                .basePriceNet(l.getBasePriceNet().negate()).baseAmount(l.getBaseAmount().negate())
                .vatAmount(l.getVatAmount().negate()).recargoAmount(l.getRecargoAmount().negate())
                .subtotal(l.getSubtotal().negate()).sale(negativeSale)
                .build()).collect(Collectors.toList());
        negativeSale.setLines(negLines);

        Sale savedNegative = saleRepository.save(negativeSale);

        Invoice rectificative = createInvoice(savedNegative);

        originalInvoice.setStatus(Invoice.InvoiceStatus.RECTIFIED);
        originalInvoice.setRectifiedBy(rectificative);
        invoiceRepository.save(originalInvoice);

        return rectificative;
    }
}


