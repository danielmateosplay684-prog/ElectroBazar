package com.proconsi.electrobazar.controller.api;

import com.proconsi.electrobazar.dto.PromotionCalcRequest;
import com.proconsi.electrobazar.dto.PromotionCalcResponse;
import com.proconsi.electrobazar.model.Promotion;
import com.proconsi.electrobazar.service.PromotionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Promotion management operations (NxM rules).
 */
@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
@Slf4j
public class PromotionApiRestController {

    private final PromotionService promotionService;

    /** Lists all active promotions. */
    @GetMapping
    public List<Promotion> getAll() {
        return promotionService.findAll();
    }

    /** Returns promotion details. */
    @GetMapping("/{id}")
    public Promotion getById(@PathVariable Long id) {
        return promotionService.findById(id);
    }

    /** Creates or updates a promotion. */
    @PostMapping
    public Promotion save(@RequestBody Promotion promotion) {
        log.info("CREANDO Promoción: name={}, productos={}, categorías={}",
            promotion.getName(),
            promotion.getRestrictedProducts() != null ? promotion.getRestrictedProducts().stream().map(p -> p.getId()).collect(java.util.stream.Collectors.toList()) : "null",
            promotion.getRestrictedCategories() != null ? promotion.getRestrictedCategories().stream().map(c -> c.getId()).collect(java.util.stream.Collectors.toList()) : "null"
        );
        return promotionService.save(promotion);
    }

    /** Updates an existing promotion. */
    @PutMapping
    public Promotion update(@RequestBody Promotion promotion) {
        log.info("Promoción recibida: id={}, productos={}, categorías={}", 
            promotion.getId(),
            promotion.getRestrictedProducts() != null ? promotion.getRestrictedProducts().stream().map(p -> p.getId()).collect(java.util.stream.Collectors.toList()) : "null",
            promotion.getRestrictedCategories() != null ? promotion.getRestrictedCategories().stream().map(c -> c.getId()).collect(java.util.stream.Collectors.toList()) : "null"
        );
        return promotionService.save(promotion);
    }

    /** Deletes a promotion by ID. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        promotionService.delete(id);
        return ResponseEntity.ok().build();
    }

    /** Calculates totals for a proposed cart in the TPV. */
    @PostMapping("/calculate")
    public PromotionCalcResponse calculate(@RequestBody PromotionCalcRequest request) {
        return promotionService.calculateTotals(request);
    }
}
