package com.proconsi.electrobazar.scheduler;

import com.proconsi.electrobazar.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ActivityLogScheduler {

    private final ActivityLogRepository activityLogRepository;

    /**
     * Daily maintenance of the activity log.
     * Deletes operational logs older than 90 days and audit logs older than 365 days.
     * Runs every day at 02:00 AM.
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupActivityLogs() {
        log.info("Starting Activity Log cleanup task...");

        LocalDateTime ninetyDaysAgo = LocalDateTime.now().minusDays(90);
        LocalDateTime oneYearAgo = LocalDateTime.now().minusDays(365);

        List<String> operationalActions = Arrays.asList(
                "VENTA",
                "INICIAR_SESION",
                "AJUSTE_STOCK",
                "MODIFICAR_RECARGO"
        );

        try {
            // 1. Delete operational events older than 90 days
            activityLogRepository.deleteByTimestampBeforeAndActionIn(ninetyDaysAgo, operationalActions);
            log.info("Deleted operational logs older than 90 days (before {})", ninetyDaysAgo);

            // 2. Delete all other events (audit) older than 365 days
            activityLogRepository.deleteByTimestampBeforeAndActionNotIn(oneYearAgo, operationalActions);
            log.info("Deleted audit logs older than 365 days (before {})", oneYearAgo);

            log.info("Activity Log cleanup task completed successfully.");
        } catch (Exception e) {
            log.error("Error during Activity Log cleanup: {}", e.getMessage(), e);
        }
    }
}
