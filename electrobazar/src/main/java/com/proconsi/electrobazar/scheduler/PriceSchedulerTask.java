package com.proconsi.electrobazar.scheduler;

import com.proconsi.electrobazar.model.ProductPrice;
import com.proconsi.electrobazar.repository.ProductPriceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.proconsi.electrobazar.service.IneApiService;

/**
 * Scheduled task component for daily price transition verification and cache
 * management.
 *
 * <p>
 * This scheduler runs daily at midnight (00:00:00 Europe/Madrid) to:
 * </p>
 * <ol>
 * <li>Log all price transitions that became active today (useful for audit
 * trails).</li>
 * <li>Log all upcoming price changes scheduled for the next 7 days (early
 * warning).</li>
 * <li>Evict the entire {@code productPrices} cache to ensure all price lookups
 * after midnight use fresh data from the database.</li>
 * <li>On January 1st: Fetches IPC from INE and logs a reminder if no updates
 * are scheduled.</li>
 * </ol>
 *
 * <h3>Why cache eviction at midnight?</h3>
 * <p>
 * The {@code @Cacheable} on {@code getCurrentPrice} caches prices by productId.
 * When a scheduled price transition occurs (e.g., a new year price increase),
 * the
 * cached value for that product would still return the old price until the
 * cache
 * entry expires or is evicted. Running this task at midnight ensures all caches
 * are cleared precisely when price transitions are most likely to occur.
 * </p>
 *
 * <h3>Cron expression</h3>
 * <p>
 * {@code "0 0 0 * * *"} = At 00:00:00 every day.
 * The application timezone is set to Europe/Madrid in
 * {@code ElectrobazarApplication}.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PriceSchedulerTask {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final ProductPriceRepository productPriceRepository;
    private final CacheManager cacheManager;
    private final IneApiService ineApiService;

    /**
     * Daily midnight task: verifies price transitions and evicts the price cache.
     *
     * <p>
     * Scheduled to run at 00:00:00 every day using the application's default
     * timezone (Europe/Madrid).
     * </p>
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional(readOnly = true)
    public void verifyDailyPriceTransitions() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();

        log.info("=== [PriceScheduler] Daily price verification started at {} ===",
                now.format(DATE_FORMATTER));

        // ── 1. Log prices that became active today ─────────────────────────────
        LocalDateTime dayStart = today.atStartOfDay();
        LocalDateTime dayEnd = today.atTime(23, 59, 59);

        List<ProductPrice> activatedToday = productPriceRepository.findPricesActivatedBetween(dayStart, dayEnd);

        if (activatedToday.isEmpty()) {
            log.info("[PriceScheduler] No price transitions scheduled for today ({}).",
                    today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

            // ── 1.1 Annual IPC Update Reminder (Jan 1st only) ──────────────────
            if (today.getMonthValue() == 1 && today.getDayOfMonth() == 1) {
                checkAndLogIpcReminder();
            }
        } else {
            log.info("[PriceScheduler] {} price transition(s) active today:",
                    activatedToday.size());
            activatedToday.forEach(p -> log.info(
                    "  → Product: '{}' (id={}) | New price: {} € | VAT: {}% | Effective: {} | Label: {}",
                    p.getProduct().getName(),
                    p.getProduct().getId(),
                    p.getPrice(),
                    p.getVatRate().multiply(new java.math.BigDecimal("100")).stripTrailingZeros().toPlainString(),
                    p.getStartDate().format(DATE_FORMATTER),
                    p.getLabel() != null ? p.getLabel() : "N/A"));
        }

        // ── 2. Log upcoming price changes in the next 7 days ──────────────────
        LocalDateTime sevenDaysFromNow = now.plusDays(7);
        List<ProductPrice> upcomingPrices = productPriceRepository.findAllFuturePrices(now)
                .stream()
                .filter(p -> p.getStartDate().isBefore(sevenDaysFromNow))
                .toList();

        if (!upcomingPrices.isEmpty()) {
            log.info("[PriceScheduler] {} upcoming price change(s) in the next 7 days:",
                    upcomingPrices.size());
            upcomingPrices.forEach(p -> log.info(
                    "  ⏰ Product: '{}' (id={}) | Price: {} € | Effective: {} | Label: {}",
                    p.getProduct().getName(),
                    p.getProduct().getId(),
                    p.getPrice(),
                    p.getStartDate().format(DATE_FORMATTER),
                    p.getLabel() != null ? p.getLabel() : "N/A"));
        }

        // ── 3. Evict the entire productPrices cache ────────────────────────────
        evictProductPricesCache();

        log.info("=== [PriceScheduler] Daily verification completed. Cache evicted. ===");
    }

    /**
     * Checks the latest IPC and logs a reminder if no price updates are scheduled
     * for the new year.
     */
    private void checkAndLogIpcReminder() {
        try {
            java.math.BigDecimal ipc = ineApiService.getLatestIpc();
            if (ipc != null) {
                log.warn("🔔 [PriceScheduler] RECORDATORIO ANUAL: El IPC actual es del {}%. " +
                        "No se han detectado actualizaciones de precio programadas para hoy. " +
                        "Considere aplicar una actualización masiva en el panel de administración.", ipc);
            }
        } catch (Exception e) {
            log.error("[PriceScheduler] Error fetching IPC for annual reminder: {}", e.getMessage());
        }
    }

    /**
     * Evicts all entries from the {@code productPrices} cache.
     *
     * <p>
     * This ensures that after midnight, all subsequent calls to
     * {@code ProductPriceService.getCurrentPrice()} will fetch fresh data
     * from the database, picking up any newly activated price transitions.
     * </p>
     */
    private void evictProductPricesCache() {
        var cache = cacheManager.getCache("productPrices");
        if (cache != null) {
            cache.clear();
            log.info("[PriceScheduler] 'productPrices' cache cleared successfully.");
        } else {
            log.warn("[PriceScheduler] Cache 'productPrices' not found in CacheManager.");
        }
    }

    /**
     * Additional task: runs every hour to log a summary of all future scheduled
     * prices.
     * This is a lightweight monitoring task that helps operators stay informed.
     *
     * <p>
     * Scheduled to run at minute 0 of every hour (e.g., 01:00, 02:00, ...).
     * </p>
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional(readOnly = true)
    public void logFuturePricesSummary() {
        LocalDateTime now = LocalDateTime.now();
        List<ProductPrice> futurePrices = productPriceRepository.findAllFuturePrices(now);

        if (!futurePrices.isEmpty()) {
            log.debug("[PriceScheduler] {} future price(s) scheduled across all products.",
                    futurePrices.size());
        }
    }

}
