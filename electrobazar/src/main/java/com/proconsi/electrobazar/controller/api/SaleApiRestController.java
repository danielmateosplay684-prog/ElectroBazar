package com.proconsi.electrobazar.controller.api;

import com.proconsi.electrobazar.model.Product;
import com.proconsi.electrobazar.model.Sale;
import com.proconsi.electrobazar.model.SaleLine;
import com.proconsi.electrobazar.model.Worker;
import com.proconsi.electrobazar.model.Customer;
import com.proconsi.electrobazar.service.ProductService;
import com.proconsi.electrobazar.service.SaleService;
import com.proconsi.electrobazar.service.CustomerService;
import com.proconsi.electrobazar.service.WorkerService;
import com.proconsi.electrobazar.dto.SaleSummaryResponse;
import com.proconsi.electrobazar.dto.AnalyticsSummaryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

/**
 * REST Controller for managing standard sales.
 * Handles sale retrieval, filtering by date range, and basic sale creation.
 */
@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SaleApiRestController {

    private final SaleService saleService;
    private final ProductService productService;
    private final CustomerService customerService;
    private final WorkerService workerService;

    /**
     * Retrieves all recorded sales with pagination.
     * Returns a lightweight DTO to avoid N+1 queries for lines and products.
     * @param pageable Pagination and sorting criteria.
     * @return Paginated result containing {@link com.proconsi.electrobazar.dto.SaleListingDTO}.
     */
    @GetMapping
    public ResponseEntity<org.springframework.data.domain.Page<com.proconsi.electrobazar.dto.SaleListingDTO>> getAll(
            @org.springframework.data.web.PageableDefault(size = 20, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) org.springframework.data.domain.Pageable pageable) {
        
        org.springframework.data.domain.Page<Sale> sales = saleService.findAll(pageable);
        org.springframework.data.domain.Page<com.proconsi.electrobazar.dto.SaleListingDTO> dtos = sales.map(s -> com.proconsi.electrobazar.dto.SaleListingDTO.builder()
                .id(s.getId())
                .createdAt(s.getCreatedAt())
                .customerName(s.getCustomer() != null ? s.getCustomer().getName() : null)
                .workerUsername(s.getWorker() != null ? s.getWorker().getUsername() : null)
                .paymentMethod(s.getPaymentMethod() != null ? s.getPaymentMethod().name() : null)
                .totalAmount(s.getTotalAmount())
                .status(s.getStatus() != null ? s.getStatus().name() : "ACTIVE")
                .invoiceNumber(s.getInvoice() != null ? s.getInvoice().getInvoiceNumber() : null)
                .ticketNumber(s.getTicket() != null ? s.getTicket().getTicketNumber() : null)
                .build());
                
        return ResponseEntity.ok(dtos);
    }

    /**
     * Retrieves a single sale by its ID.
     * @param id Internal sale ID.
     * @return The requested {@link Sale}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Sale> getById(@PathVariable Long id) {
        return ResponseEntity.ok(saleService.findById(id));
    }

    /**
     * Retrieves all sales performed during the current day.
     * @return List of today's sales.
     */
    @GetMapping("/today")
    public ResponseEntity<List<Sale>> getToday() {
        return ResponseEntity.ok(saleService.findToday());
    }

    /**
     * Retrieves a statistical summary of today's sales (totals, counts, etc.).
     * @return {@link SaleSummaryResponse} data.
     */
    @GetMapping("/stats/today")
    public ResponseEntity<SaleSummaryResponse> getTodayStats() {
        return ResponseEntity.ok(saleService.getSummaryToday());
    }

    /**
     * Filters sales within a specific date and time range.
     * @param from Start date-time (ISO format).
     * @param to End date-time (ISO format).
     * @return List of sales in the specified range.
     */
    @GetMapping("/range")
    public ResponseEntity<List<Sale>> getRange(
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(saleService.findBetween(from, to));
    }

    /**
     * Filters sales within a specific date and time range with pagination.
     * @param from Start date-time (ISO format).
     * @param to End date-time (ISO format).
     * @param workerId Optional worker ID to filter by.
     * @param pageable Pagination and sorting criteria.
     * @return Paginated sales in the specified range.
     */
    @GetMapping("/range/paged")
    public ResponseEntity<Page<Sale>> getRangePaged(
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(value = "workerId", required = false) Long workerId,
            @PageableDefault(size = 50, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        
        if (workerId != null) {
            return ResponseEntity.ok(saleService.findBetween(from, to, workerId, pageable));
        }
        return ResponseEntity.ok(saleService.findBetween(from, to, pageable));
    }

    /**
     * Retrieves aggregated analytics for the specified period.
     * @return {@link AnalyticsSummaryDTO} pre-calculated statistics.
     */
    @GetMapping("/analytics")
    public ResponseEntity<AnalyticsSummaryDTO> getAnalytics(
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(saleService.getAnalyticsSummary(from, to));
    }

    /**
     * Creates a new sale.
     * Note: For advanced tax and temporal pricing, use the '/api/sales/with-tax' endpoint instead.
     * 
     * @param sale The sale data (lines, customer info, payment method).
     * @param workerId ID of the worker performing the sale (from header).
     * @return The saved {@link Sale} entity.
     */
    @PostMapping
    public ResponseEntity<Sale> create(
            @RequestBody Sale sale,
            @RequestHeader(value = "X-Worker-Id", required = false) Long workerId) {

        Worker worker = null;
        if (workerId != null) {
            worker = workerService.findById(workerId).orElse(null);
        }

        List<SaleLine> lines = sale.getLines().stream().map(line -> {
            Product product = productService.findById(line.getProduct().getId());
            return SaleLine.builder()
                    .product(product)
                    .quantity(line.getQuantity())
                    .unitPrice(product.getPrice())
                    .build();
        }).collect(Collectors.toList());

        Customer validCustomer = null;
        if (sale.getCustomer() != null && sale.getCustomer().getName() != null
                && !sale.getCustomer().getName().isBlank()) {
            Customer newCust = Customer.builder()
                    .name(sale.getCustomer().getName())
                    .type(sale.getCustomer().getType() != null ? sale.getCustomer().getType()
                            : Customer.CustomerType.INDIVIDUAL)
                    .build();
            validCustomer = customerService.save(newCust);
        }

        Sale saved;
        if (validCustomer != null) {
            saved = saleService.createSale(lines, sale.getPaymentMethod(), sale.getNotes(),
                    sale.getReceivedAmount(), sale.getCashAmount(), sale.getCardAmount(), validCustomer, worker);
        } else {
            saved = saleService.createSale(lines, sale.getPaymentMethod(), sale.getNotes(),
                    sale.getReceivedAmount(), sale.getCashAmount(), sale.getCardAmount(), worker);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * Cancels an existing sale.
     * @param id The sale ID to cancel.
     * @param body Request body containing the reason for cancellation.
     * @param workerId ID of the worker authorizing the cancellation.
     * @return 200 OK.
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "X-Worker-Id", required = false) Long workerId) {

        Worker worker = null;
        if (workerId != null) {
            worker = workerService.findById(workerId).orElse(null);
        }
        String reason = body.getOrDefault("reason", "Anulación desde API");
        saleService.cancelSale(id, worker, reason);
        return ResponseEntity.ok().build();
    }
}
