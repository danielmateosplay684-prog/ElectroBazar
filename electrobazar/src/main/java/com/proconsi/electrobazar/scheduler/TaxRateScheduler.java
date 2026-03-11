package com.proconsi.electrobazar.scheduler;

import com.proconsi.electrobazar.model.TaxRate;
import com.proconsi.electrobazar.repository.TaxRateRepository;
import com.proconsi.electrobazar.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaxRateScheduler {

    private final TaxRateRepository taxRateRepository;
    private final ProductService productService;

    /**
     * Checks daily at 00:01 if there are any new TaxRates that should be applied starting today.
     * Cron: "0 1 0 * * *" (Seconds Minutes Hours Day Month DayOfWeek)
     */
    @Scheduled(cron = "0 1 0 * * *")
    public void autoApplyTaxRates() {
        LocalDate today = LocalDate.now();
        log.info("Checking for new TaxRates starting today ({})", today);

        List<TaxRate> newRates = taxRateRepository.findByValidFromAndActiveTrue(today);

        if (newRates.isEmpty()) {
            log.info("No new TaxRates to apply today.");
            return;
        }

        for (TaxRate rate : newRates) {
            try {
                log.info("Auto-applying TaxRate: {} ({}%)", rate.getDescription(), rate.getVatRate());
                productService.applyNewTaxRate(rate.getId());
                log.info("Successfully applied TaxRate: {}", rate.getDescription());
            } catch (Exception e) {
                log.error("Error auto-applying TaxRate {}: {}", rate.getId(), e.getMessage());
            }
        }
    }
}
