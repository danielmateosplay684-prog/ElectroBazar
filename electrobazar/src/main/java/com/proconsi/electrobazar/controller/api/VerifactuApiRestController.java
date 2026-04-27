package com.proconsi.electrobazar.controller.api;

import com.proconsi.electrobazar.model.AeatStatus;
import com.proconsi.electrobazar.model.Invoice;
import com.proconsi.electrobazar.model.RectificativeInvoice;
import com.proconsi.electrobazar.model.Ticket;
import com.proconsi.electrobazar.repository.InvoiceRepository;
import com.proconsi.electrobazar.repository.RectificativeInvoiceRepository;
import com.proconsi.electrobazar.repository.TicketRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API for VeriFactu / AEAT submission status dashboard.
 * Covers invoices (facturas), rectificative invoices (facturas rectificativas)
 * and simplified tickets (tickets).
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/verifactu")
public class VerifactuApiRestController {

    private final InvoiceRepository              invoiceRepository;
    private final RectificativeInvoiceRepository rectificativeRepository;
    private final TicketRepository               ticketRepository;

    /* ── SUMMARY ──────────────────────────────────────────────── */

    @GetMapping("/summary")
    public ResponseEntity<?> summary(HttpSession session) {
        if (!Boolean.TRUE.equals(session.getAttribute("admin")))
            return ResponseEntity.status(401).build();

        List<Invoice>              invoices  = invoiceRepository.findAll();
        List<RectificativeInvoice> rectifs   = rectificativeRepository.findAll();
        List<Ticket>               tickets   = ticketRepository.findAll();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("invoices",      kpis(invoices,  i -> i.getAeatStatus()));
        result.put("rectificativas",kpis(rectifs,   r -> r.getAeatStatus()));
        result.put("tickets",       kpis(tickets,   t -> t.getAeatStatus()));
        return ResponseEntity.ok(result);
    }

    @FunctionalInterface
    private interface StatusExtractor<T> {
        AeatStatus get(T item);
    }

    private <T> Map<String, Long> kpis(List<T> list, StatusExtractor<T> fn) {
        Map<String, Long> m = new LinkedHashMap<>();
        m.put("total",             (long) list.size());
        m.put("accepted",          list.stream().filter(i -> fn.get(i) == AeatStatus.ACCEPTED).count());
        m.put("acceptedWithError", list.stream().filter(i -> fn.get(i) == AeatStatus.ACCEPTED_WITH_ERRORS).count());
        m.put("rejected",          list.stream().filter(i -> fn.get(i) == AeatStatus.REJECTED).count());
        m.put("annulled",          list.stream().filter(i -> fn.get(i) == AeatStatus.ANNULLED).count());
        m.put("pending",           list.stream().filter(i -> fn.get(i) == AeatStatus.PENDING_SEND).count());
        m.put("notSent",           list.stream().filter(i -> fn.get(i) == null).count());
        return m;
    }

    /* ── INVOICES ─────────────────────────────────────────────── */

    @GetMapping("/invoices")
    public ResponseEntity<?> invoices(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false)    String status,
            HttpSession session) {

        if (!Boolean.TRUE.equals(session.getAttribute("admin")))
            return ResponseEntity.status(401).build();

        List<Invoice> all = invoiceRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Invoice> filtered = filterByStatus(all, i -> i.getAeatStatus(), status);

        List<Map<String, Object>> rows = new ArrayList<>();
        for (Invoice inv : paginate(filtered, page, size)) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id",             inv.getId());
            row.put("number",         inv.getInvoiceNumber());
            row.put("type",           "FACTURA");
            row.put("invoiceStatus",  inv.getStatus() != null ? inv.getStatus().name() : null);
            row.put("saleId",         inv.getSale() != null ? inv.getSale().getId() : null);
            row.put("returnId",       null);
            row.put("createdAt",      inv.getCreatedAt() != null ? inv.getCreatedAt().toString() : null);
            row.put("aeatStatus",     inv.getAeatStatus() != null ? inv.getAeatStatus().name() : "NOT_SENT");
            row.put("submissionDate", inv.getAeatSubmissionDate() != null ? inv.getAeatSubmissionDate().toString() : null);
            row.put("retryCount",     inv.getAeatRetryCount());
            row.put("lastError",      inv.getAeatLastError());
            row.put("hash",           inv.getHashCurrentInvoice());
            rows.add(row);
        }
        return ResponseEntity.ok(pageResponse(rows, filtered.size(), page, size));
    }

    /* ── RECTIFICATIVAS ───────────────────────────────────────── */

    @GetMapping("/rectificativas")
    public ResponseEntity<?> rectificativas(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false)    String status,
            HttpSession session) {

        if (!Boolean.TRUE.equals(session.getAttribute("admin")))
            return ResponseEntity.status(401).build();

        List<RectificativeInvoice> all = rectificativeRepository.findAllWithDetails(
                Sort.by(Sort.Direction.DESC, "createdAt"));
        List<RectificativeInvoice> filtered = filterByStatus(all, r -> r.getAeatStatus(), status);

        List<Map<String, Object>> rows = new ArrayList<>();
        for (RectificativeInvoice r : paginate(filtered, page, size)) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id",             r.getId());
            row.put("number",         r.getRectificativeNumber());
            row.put("type",           "RECTIFICATIVA");
            row.put("invoiceStatus",  null);
            row.put("saleId",         r.getSaleReturn() != null && r.getSaleReturn().getOriginalSale() != null
                                        ? r.getSaleReturn().getOriginalSale().getId() : null);
            row.put("returnId",       r.getSaleReturn() != null ? r.getSaleReturn().getId() : null);
            row.put("originalNumber", r.getOriginalInvoice() != null ? r.getOriginalInvoice().getInvoiceNumber() : null);
            row.put("reason",         r.getReason());
            row.put("createdAt",      r.getCreatedAt() != null ? r.getCreatedAt().toString() : null);
            row.put("aeatStatus",     r.getAeatStatus() != null ? r.getAeatStatus().name() : "NOT_SENT");
            row.put("submissionDate", r.getAeatSubmissionDate() != null ? r.getAeatSubmissionDate().toString() : null);
            row.put("retryCount",     r.getAeatRetryCount());
            row.put("lastError",      r.getAeatLastError());
            row.put("hash",           r.getHashCurrentInvoice());
            rows.add(row);
        }
        return ResponseEntity.ok(pageResponse(rows, filtered.size(), page, size));
    }

    /* ── TICKETS ──────────────────────────────────────────────── */

    @GetMapping("/tickets")
    public ResponseEntity<?> tickets(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false)    String status,
            HttpSession session) {

        if (!Boolean.TRUE.equals(session.getAttribute("admin")))
            return ResponseEntity.status(401).build();

        List<Ticket> all = ticketRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Ticket> filtered = filterByStatus(all, t -> t.getAeatStatus(), status);

        List<Map<String, Object>> rows = new ArrayList<>();
        for (Ticket t : paginate(filtered, page, size)) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id",             t.getId());
            row.put("number",         t.getTicketNumber());
            row.put("type",           "TICKET");
            row.put("invoiceStatus",  null);
            row.put("saleId",         t.getSale() != null ? t.getSale().getId() : null);
            row.put("returnId",       null);
            row.put("createdAt",      t.getCreatedAt() != null ? t.getCreatedAt().toString() : null);
            row.put("aeatStatus",     t.getAeatStatus() != null ? t.getAeatStatus().name() : "NOT_SENT");
            row.put("submissionDate", t.getAeatSubmissionDate() != null ? t.getAeatSubmissionDate().toString() : null);
            row.put("retryCount",     t.getAeatRetryCount());
            row.put("lastError",      t.getAeatLastError());
            row.put("hash",           t.getHashCurrentInvoice());
            rows.add(row);
        }
        return ResponseEntity.ok(pageResponse(rows, filtered.size(), page, size));
    }

    /* ── HELPERS ──────────────────────────────────────────────── */

    private <T> List<T> filterByStatus(List<T> list, StatusExtractor<T> fn, String status) {
        if (status == null || status.isBlank() || "ALL".equalsIgnoreCase(status))
            return list;
        if ("NOT_SENT".equalsIgnoreCase(status))
            return list.stream().filter(i -> fn.get(i) == null).toList();
        try {
            AeatStatus s = AeatStatus.valueOf(status);
            return list.stream().filter(i -> s.equals(fn.get(i))).toList();
        } catch (Exception e) {
            return list;
        }
    }

    private <T> List<T> paginate(List<T> list, int page, int size) {
        int start = Math.min(page * size, list.size());
        int end   = Math.min(start + size, list.size());
        return list.subList(start, end);
    }

    private Map<String, Object> pageResponse(List<Map<String, Object>> rows, int total, int page, int size) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("content",       rows);
        m.put("totalElements", total);
        m.put("totalPages",    size > 0 ? (int) Math.ceil((double) total / size) : 1);
        m.put("page",          page);
        m.put("size",          size);
        return m;
    }
}
