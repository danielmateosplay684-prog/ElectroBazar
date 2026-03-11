package com.proconsi.electrobazar.service;

import com.proconsi.electrobazar.model.TariffPriceHistory;
import java.util.List;

import com.proconsi.electrobazar.dto.TariffPriceEntryDTO;

public interface TariffPriceHistoryService {
    List<TariffPriceHistory> getHistoryByTariff(Long tariffId);
    List<TariffPriceHistory> getHistoryByProduct(Long productId);
    List<TariffPriceEntryDTO> getCurrentPricesForTariff(Long tariffId);
}
