package com.proconsi.electrobazar.service;

import com.proconsi.electrobazar.model.TariffPriceHistory;
import com.proconsi.electrobazar.dto.TariffPriceEntryDTO;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Interface for querying historical pricing data tied to specific tariffs.
 */
public interface TariffPriceHistoryService {

    /**
     * Retrieves the price evolution for a whole tariff.
     * @param tariffId ID of the tariff.
     * @return List of historical price records.
     */
    List<TariffPriceHistory> getHistoryByTariff(Long tariffId);

    /**
     * Retrieves price changes for a specific product across all tariffs.
     * @param productId ID of the product.
     * @return List of historical price records.
     */
    List<TariffPriceHistory> getHistoryByProduct(Long productId);

    /**
     * Retrieves the prices currently in effect for a given tariff.
     * @param tariffId ID of the tariff.
     * @return List of active price DTOs.
     */
    Page<TariffPriceEntryDTO> getCurrentPricesForTariff(Long tariffId, Pageable pageable);

    /**
     * Lists distinct dates when price changes were applied to a tariff.
     * @param tariffId ID of the tariff.
     * @return List of LocalDate objects.
     */
    List<LocalDate> getDistinctValidFromDates(Long tariffId);

    /**
     * Snapshots the prices for a tariff as they were on a specific date.
     * @param tariffId ID of the tariff.
     * @param date     Point-in-time calculation date.
     * @return List of prices for that date.
     */
    Page<TariffPriceEntryDTO> getPricesForTariffAtDate(Long tariffId, LocalDate date, Pageable pageable);

    List<TariffPriceEntryDTO> getPricesForTariffAtDateList(Long tariffId, LocalDate date);

    void generateInitialSnapshotIfEmpty(Long tariffId);

    boolean isInitializationInProgress(Long tariffId);
}
