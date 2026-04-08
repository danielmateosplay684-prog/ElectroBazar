package com.proconsi.electrobazar.service;

import com.proconsi.electrobazar.dto.PromotionCalcRequest;
import com.proconsi.electrobazar.dto.PromotionCalcResponse;
import com.proconsi.electrobazar.model.MeasurementUnit;
import com.proconsi.electrobazar.model.Product;
import com.proconsi.electrobazar.model.Promotion;
import com.proconsi.electrobazar.repository.ProductRepository;
import com.proconsi.electrobazar.repository.PromotionRepository;
import com.proconsi.electrobazar.service.impl.PromotionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromotionServiceTest {

        @Mock
        private PromotionRepository promotionRepository;

        @Mock
        private ProductRepository productRepository;

        @InjectMocks
        private PromotionServiceImpl promotionService;

        private Product unitProduct;
        private Product weightProduct;
        private Promotion promo3x2;

        @BeforeEach
        void setUp() {
                // Unit product (0 decimals)
                MeasurementUnit unitUd = MeasurementUnit.builder()
                                .name("Unidad")
                                .symbol("ud.")
                                .decimalPlaces(0)
                                .build();
                unitProduct = Product.builder()
                                .id(1L)
                                .nameEs("Producto Unitario")
                                .price(new BigDecimal("10.00"))
                                .measurementUnit(unitUd)
                                .build();

                // Weight product (3 decimals)
                MeasurementUnit unitKg = MeasurementUnit.builder()
                                .name("Kilogramo")
                                .symbol("kg")
                                .decimalPlaces(3)
                                .build();
                weightProduct = Product.builder()
                                .id(2L)
                                .nameEs("Producto Peso")
                                .price(new BigDecimal("20.00"))
                                .measurementUnit(unitKg)
                                .build();

                // Promo 3x2 (Buy 3, Pay 2)
                promo3x2 = Promotion.builder()
                                .id(1L)
                                .name("Oferta 3x2")
                                .active(true)
                                .nValue(3)
                                .mValue(2)
                                .build();
        }

        @Test
        @DisplayName("3x2 sobre 6 unidades: Debería descontar 2 unidades")
        void testPromo3x2SixUnits() {
                when(promotionRepository.findAllActive()).thenReturn(Arrays.asList(promo3x2));
                when(productRepository.findById(1L)).thenReturn(Optional.of(unitProduct));

                PromotionCalcRequest request = PromotionCalcRequest.builder()
                                .lines(Arrays.asList(
                                                PromotionCalcRequest.Line.builder().productId(1L)
                                                                .quantity(new BigDecimal("6")).build()))
                                .build();

                PromotionCalcResponse response = promotionService.calculateTotals(request);

                // Subtotal = 6 * 10 = 60.00
                // Promo 3x2: floorBy(6, 3) = 2 grupos -> 2 unidades gratis = 20.00 de descuento
                assertEquals(0, new BigDecimal("20.00").compareTo(response.getTotalDiscount()));
        }

        @Test
        @DisplayName("3x2 sobre 9 unidades: Debería descontar 3 unidades de regalo")
        void testPromo3x2NineUnits() {
                when(promotionRepository.findAllActive()).thenReturn(Arrays.asList(promo3x2));
                when(productRepository.findById(1L)).thenReturn(Optional.of(unitProduct));

                PromotionCalcRequest request = PromotionCalcRequest.builder()
                                .lines(Arrays.asList(
                                                PromotionCalcRequest.Line.builder().productId(1L)
                                                                .quantity(new BigDecimal("9")).build()))
                                .build();

                PromotionCalcResponse response = promotionService.calculateTotals(request);

                // 9 / 3 = 3 grupos -> 3 unidades gratis = 30.00 descuento
                assertEquals(0, new BigDecimal("30.00").compareTo(response.getTotalDiscount()));
        }

        @Test
        @DisplayName("3x2 sobre 1,500 kg: Debería omitirse por ser fraccionario")
        void testPromo3x2WeightProduct() {
                when(promotionRepository.findAllActive()).thenReturn(Arrays.asList(promo3x2));
                when(productRepository.findById(2L)).thenReturn(Optional.of(weightProduct));

                PromotionCalcRequest request = PromotionCalcRequest.builder()
                                .lines(Arrays.asList(
                                                PromotionCalcRequest.Line.builder().productId(2L)
                                                                .quantity(new BigDecimal("1.500")).build()))
                                .build();

                PromotionCalcResponse response = promotionService.calculateTotals(request);

                // El producto tiene decimalPlaces=3, se debe omitir
                assertEquals(0, BigDecimal.ZERO.compareTo(response.getTotalDiscount()));
        }

        @Test
        @DisplayName("3x2 sobre 2 unidades: No alcanza el mínimo de 3, descuento 0")
        void testPromo3x2UnderLimit() {
                when(promotionRepository.findAllActive()).thenReturn(Arrays.asList(promo3x2));
                when(productRepository.findById(1L)).thenReturn(Optional.of(unitProduct));

                PromotionCalcRequest request = PromotionCalcRequest.builder()
                                .lines(Arrays.asList(
                                                PromotionCalcRequest.Line.builder().productId(1L)
                                                                .quantity(new BigDecimal("2")).build()))
                                .build();

                PromotionCalcResponse response = promotionService.calculateTotals(request);

                assertEquals(0, BigDecimal.ZERO.compareTo(response.getTotalDiscount()));
        }
}
