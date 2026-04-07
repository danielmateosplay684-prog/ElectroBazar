package com.proconsi.electrobazar.service;

import com.proconsi.electrobazar.model.Promotion;
import com.proconsi.electrobazar.model.SaleLine;
import java.util.List;

public interface PromotionService {
    List<SaleLine> applyNxMPromotions(List<SaleLine> lines);

    /**
     * Calculates the total discount from automatic promotions for a set of items.
     * Useful for previewing the discount in the TPV before the sale is completed.
     * 
     * @param request The ticket composition details.
     * @return Details about the calculated discount.
     */
    com.proconsi.electrobazar.dto.PromotionCalcResponse calculateTotals(com.proconsi.electrobazar.dto.PromotionCalcRequest request);

    /**
     * Finds all promotions.
     */
    List<Promotion> findAll();

    /**
     * Finds a specific promotion by ID.
     */
    Promotion findById(Long id);

    /**
     * Creates or updates a promotion.
     */
    Promotion save(Promotion promotion);

    /**
     * Deletes a promotion by ID.
     */
    void delete(Long id);
}
