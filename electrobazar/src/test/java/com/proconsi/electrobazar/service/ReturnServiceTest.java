package com.proconsi.electrobazar.service;

import com.proconsi.electrobazar.dto.ReturnLineRequest;
import com.proconsi.electrobazar.model.*;
import com.proconsi.electrobazar.repository.*;
import com.proconsi.electrobazar.service.impl.ReturnServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReturnServiceTest {

    @Mock private SaleReturnRepository saleReturnRepository;
    @Mock private ReturnLineRepository returnLineRepository;
    @Mock private SaleLineRepository saleLineRepository;
    @Mock private SaleService saleService;
    @Mock private ProductService productService;
    @Mock private InvoiceService invoiceService;
    @Mock private CashRegisterService cashRegisterService;
    @Mock private ActivityLogService activityLogService;
    @Mock private RectificativeInvoiceRepository rectificativeInvoiceRepository;
    @Mock private InvoiceSequenceRepository invoiceSequenceRepository;
    @Mock private InvoiceRepository invoiceRepository;

    @InjectMocks
    private ReturnServiceImpl returnService;

    private Sale originalSale;
    private SaleLine line;
    private Worker worker;

    @BeforeEach
    void setUp() {
        worker = new Worker();
        worker.setId(1L);

        Product product = Product.builder().id(50L).nameEs("Test Product").build();

        originalSale = Sale.builder()
                .id(100L)
                .totalAmount(new BigDecimal("100.00"))
                .build();

        line = SaleLine.builder()
                .id(1L)
                .sale(originalSale)
                .product(product)
                .quantity(new BigDecimal("3.500"))
                .unitPrice(new BigDecimal("10.00"))
                .vatRate(new BigDecimal("0.21"))
                .build();
        
        lenient().when(saleService.findById(100L)).thenReturn(originalSale);
        lenient().when(saleLineRepository.findById(1L)).thenReturn(Optional.of(line));
        
        // General mocks to avoid NPEs in business logic
        lenient().when(cashRegisterService.getCurrentCashBalance()).thenReturn(new BigDecimal("1000.00"));
        
        InvoiceSequence seq = InvoiceSequence.builder().serie("D").year(2026).lastNumber(0).build();
        lenient().when(invoiceSequenceRepository.findBySerieAndYearForUpdate(anyString(), anyInt())).thenReturn(Optional.of(seq));
        lenient().when(invoiceSequenceRepository.save(any(InvoiceSequence.class))).thenAnswer(i -> i.getArguments()[0]);
        
        lenient().when(rectificativeInvoiceRepository.findFirstByOrderByCreatedAtDesc()).thenReturn(Optional.empty());
    }

    @Test
    public void testExactReturnSuccess() {
        // Case 1: Return exactly 3.500 of 3.500
        ReturnLineRequest req = new ReturnLineRequest();
        req.setSaleLineId(1L);
        req.setQuantity(new BigDecimal("3.500"));

        when(returnLineRepository.sumReturnedQuantityBySaleLineId(1L)).thenReturn(BigDecimal.ZERO);
        when(saleReturnRepository.save(any(SaleReturn.class))).thenAnswer(i -> i.getArguments()[0]);

        SaleReturn result = returnService.processReturn(100L, List.of(req), "Total", PaymentMethod.CASH, worker);

        assertNotNull(result);
        assertEquals(new BigDecimal("35.00"), result.getTotalRefunded(), "Total refunded should be 3.5 * 10.0");
        verify(productService).increaseStock(any(), eq(new BigDecimal("3.500")));
    }

    @Test
    public void testPartialReturnSuccess() {
        // Case 2: Return partial 1.250 of 3.500
        ReturnLineRequest req = new ReturnLineRequest();
        req.setSaleLineId(1L);
        req.setQuantity(new BigDecimal("1.250"));

        when(returnLineRepository.sumReturnedQuantityBySaleLineId(1L)).thenReturn(BigDecimal.ZERO);
        when(saleReturnRepository.save(any(SaleReturn.class))).thenAnswer(i -> i.getArguments()[0]);

        SaleReturn result = returnService.processReturn(100L, List.of(req), "Partial 1", PaymentMethod.CASH, worker);

        assertNotNull(result);
        assertEquals(new BigDecimal("12.50"), result.getTotalRefunded());
        verify(productService).increaseStock(any(), eq(new BigDecimal("1.250")));
    }

    @Test
    public void testSecondPartialReturnSuccess() {
        // Case 3: Second partial return 1.000 after 1.250 already returned
        ReturnLineRequest req = new ReturnLineRequest();
        req.setSaleLineId(1L);
        req.setQuantity(new BigDecimal("1.000"));

        // Already returned 1.250. Available = 3.500 - 1.250 = 2.250.
        when(returnLineRepository.sumReturnedQuantityBySaleLineId(1L)).thenReturn(new BigDecimal("1.250"));
        when(saleReturnRepository.save(any(SaleReturn.class))).thenAnswer(i -> i.getArguments()[0]);

        SaleReturn result = returnService.processReturn(100L, List.of(req), "Partial 2", PaymentMethod.CASH, worker);

        assertNotNull(result);
        assertEquals(new BigDecimal("10.00"), result.getTotalRefunded());
        verify(productService).increaseStock(any(), eq(new BigDecimal("1.000")));
    }

    @Test
    public void testOverReturnThrowsException() {
        // Case 4: Try to return more than available (2.251 remaining, try to return 2.300)
        ReturnLineRequest req = new ReturnLineRequest();
        req.setSaleLineId(1L);
        req.setQuantity(new BigDecimal("2.300"));

        when(returnLineRepository.sumReturnedQuantityBySaleLineId(1L)).thenReturn(new BigDecimal("1.250"));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            returnService.processReturn(100L, List.of(req), "Overload", PaymentMethod.CASH, worker);
        });

        assertEquals("Cantidad a devolver supera la cantidad disponible", exception.getMessage());
    }

    @Test
    public void testIntegerReturnSuccess() {
        // Case 5: Standard integer case
        line.setQuantity(new BigDecimal("5.000"));
        ReturnLineRequest req = new ReturnLineRequest();
        req.setSaleLineId(1L);
        req.setQuantity(new BigDecimal("2.000"));

        when(returnLineRepository.sumReturnedQuantityBySaleLineId(1L)).thenReturn(BigDecimal.ZERO);
        when(saleReturnRepository.save(any(SaleReturn.class))).thenAnswer(i -> i.getArguments()[0]);

        SaleReturn result = returnService.processReturn(100L, List.of(req), "Integer", PaymentMethod.CASH, worker);

        assertNotNull(result);
        assertEquals(new BigDecimal("20.00"), result.getTotalRefunded());
    }
}
