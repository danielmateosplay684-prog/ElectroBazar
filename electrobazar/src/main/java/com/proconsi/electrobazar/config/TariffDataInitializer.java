package com.proconsi.electrobazar.config;

import com.proconsi.electrobazar.model.Tariff;
import com.proconsi.electrobazar.repository.TariffRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Master data initializer for Tariffs.
 * Runs on application startup (CommandLineRunner).
 * Creates default tariffs (Retail, Wholesale, Employee) if they don't exist in the database.
 * If they already exist, it ensures the 'systemTariff' flag is active.
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class TariffDataInitializer implements CommandLineRunner {

    private final TariffRepository tariffRepository;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        // Automatic patch for database: Add color column if it doesn't exist
        try {
            jdbcTemplate.execute("ALTER TABLE tariffs ADD COLUMN color VARCHAR(7)");
            log.info(">>> Database Patch: Added 'color' column to 'tariffs' table.");
        } catch (Exception e) {
            // Probably column already exists or other error, safe to ignore
        }

        seedSystemTariff(Tariff.MINORISTA, BigDecimal.ZERO,
                "Tarifa estándar para clientes minoristas. Sin descuento.", "#94a3b8");
        seedSystemTariff(Tariff.MAYORISTA, new BigDecimal("15.00"),
                "Tarifa mayorista con descuento del 15%.", "#34d399");
        seedSystemTariff(Tariff.EMPLEADO, new BigDecimal("10.00"),
                "Tarifa empleado con descuento del 10%.", "#60a5fa");
        log.info(">>> System tariffs initialized (with professional colors)");
    }

    private void seedSystemTariff(String name, BigDecimal defaultDiscount, String description, String defaultColor) {
        tariffRepository.findByName(name).ifPresentOrElse(
                t -> {
                    // Enforce the system flag even if someone removed it
                    boolean changed = false;
                    if (!Boolean.TRUE.equals(t.getSystemTariff())) {
                        t.setSystemTariff(true);
                        changed = true;
                    }
                    if (t.getColor() == null) {
                        t.setColor(defaultColor);
                        changed = true;
                    }
                    if (changed) tariffRepository.save(t);
                },
                () -> {
                    Tariff tariff = Tariff.builder()
                            .name(name)
                            .discountPercentage(defaultDiscount)
                            .description(description)
                            .color(defaultColor)
                            .active(true)
                            .systemTariff(true)
                            .build();
                    tariffRepository.save(tariff);
                    log.info("  → Created system tariff: {} ({}%)", name, defaultDiscount);
                });
    }
}
