package com.proconsi.electrobazar.controller.api;

import com.proconsi.electrobazar.dto.PromotionCalcRequest;
import com.proconsi.electrobazar.dto.PromotionCalcResponse;
import com.proconsi.electrobazar.model.Promotion;
import com.proconsi.electrobazar.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Promotion management operations (NxM rules).
 */
@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
public class PromotionApiRestController {

    private final PromotionService promotionService;

    /** Retrieves all promotions. */
    @GetMapping
    public List<Promotion> getAll() {
        return promotionService.findAll();
    }

    /** Finds a specific promotion by ID. */
    @GetMapping("/{id}")
    public Promotion getById(@PathVariable Long id) {
        return promotionService.findById(id);
    }

    /** Creates or updates a promotion. */
    @PostMapping
    public Promotion save(@RequestBody Promotion promotion) {
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
