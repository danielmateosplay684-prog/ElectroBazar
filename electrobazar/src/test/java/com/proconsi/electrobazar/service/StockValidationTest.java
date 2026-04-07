package com.proconsi.electrobazar.service;

import com.proconsi.electrobazar.exception.ResourceNotFoundException;
import com.proconsi.electrobazar.model.*;
import com.proconsi.electrobazar.repository.CashRegisterRepository;
import com.proconsi.electrobazar.repository.CouponRepository;
import com.proconsi.electrobazar.repository.SaleRepository;
import com.proconsi.electrobazar.repository.TariffRepository;
import com.proconsi.electrobazar.service.impl.SaleServiceImpl;
import com.proconsi.electrobazar.util.RecargoEquivalenciaCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class StockValidationTest {

    @Mock private SaleRepository saleRepository;
    @Mock private ProductService productService;
    @Mock private CashRegisterRepository cashRegisterRepository;
    @Mock private ActivityLogService activityLogService;
    @Mock private RecargoEquivalenciaCalculator recargoCalculator;
    @Mock private TariffRepository tariffRepository;
    @Mock private InvoiceService invoiceService;
    @Mock private CouponRepository couponRepository;
    @Mock private CashRegisterService cashRegisterService;
    @Mock private PromotionService promotionService;

    @InjectMocks
    private SaleServiceImpl saleService;

    private Product unitProduct;
    private Product weightProduct;
    private Worker worker;

    @BeforeEach
    void setUp() {
        unitProduct = Product.builder().id(1L).nameEs("Unitario").price(BigDecimal.TEN).stock(new BigDecimal("5")).build();
        weightProduct = Product.builder().id(2L).nameEs("Peso").price(BigDecimal.TEN).stock(new BigDecimal("2.500")).build();
        worker = Worker.builder().username("tester").build();

        // Mocks common to all tests to reach the validation part
        lenient().when(promotionService.applyNxMPromotions(any())).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(tariffRepository.findByName(any())).thenReturn(Optional.of(Tariff.builder().name(Tariff.MINORISTA).build()));
        
        // Mock to bypass or handle Tax breakdown (RecargoEquivalenciaCalculator)
        lenient().when(recargoCalculator.calculateLineBreakdown(any(), any(), any(), any(), any(), any(Boolean.class)))
                .thenReturn(com.proconsi.electrobazar.dto.TaxBreakdown.builder()
                        .unitPrice(BigDecimal.TEN).baseAmount(BigDecimal.TEN).vatAmount(BigDecimal.ZERO)
                        .recargoAmount(BigDecimal.ZERO).totalAmount(BigDecimal.TEN).build());
        lenient().when(saleRepository.save(any())).thenAnswer(i -> {
            Sale s = i.getArgument(0);
            s.setId(99L);
            return s;
        });
    }

    @Test
    @DisplayName("Sufficient stock: Should pass")
    void testEnoughStock() {
        unitProduct.setStock(new BigDecimal("10"));
        SaleLine line = SaleLine.builder().product(unitProduct).quantity(new BigDecimal("5")).unitPrice(BigDecimal.TEN).build();

        assertDoesNotThrow(() -> {
            saleService.createSaleWithCoupon(Collections.singletonList(line), PaymentMethod.CASH, "", 
                BigDecimal.TEN, null, null, null, worker, null, null);
        });
    }

    @Test
    @DisplayName("Exact stock: Should pass")
    void testExactStock() {
        unitProduct.setStock(new BigDecimal("5"));
        SaleLine line = SaleLine.builder().product(unitProduct).quantity(new BigDecimal("5")).unitPrice(BigDecimal.TEN).build();

        assertDoesNotThrow(() -> {
            saleService.createSaleWithCoupon(Collections.singletonList(line), PaymentMethod.CASH, "", 
                BigDecimal.TEN, null, null, null, worker, null, null);
        });
    }

    @Test
    @DisplayName("Insufficient stock: Should throw IllegalStateException")
    void testInsufficientStock() {
        unitProduct.setStock(new BigDecimal("5"));
        SaleLine line = SaleLine.builder().product(unitProduct).quantity(new BigDecimal("6")).unitPrice(BigDecimal.TEN).build();

        assertThrows(IllegalStateException.class, () -> {
            saleService.createSaleWithCoupon(Collections.singletonList(line), PaymentMethod.CASH, "", 
                BigDecimal.TEN, null, null, null, worker, null, null);
        });
    }

    @Test
    @DisplayName("Fractional insufficient stock: 2.500 vs 2.501 should throw exception")
    void testFractionalInsufficientStock() {
        weightProduct.setStock(new BigDecimal("2.500"));
        SaleLine line = SaleLine.builder().product(weightProduct).quantity(new BigDecimal("2.501")).unitPrice(BigDecimal.TEN).build();

        assertThrows(IllegalStateException.class, () -> {
            saleService.createSaleWithCoupon(Collections.singletonList(line), PaymentMethod.CASH, "", 
                BigDecimal.TEN, null, null, null, worker, null, null);
        });
    }
}
