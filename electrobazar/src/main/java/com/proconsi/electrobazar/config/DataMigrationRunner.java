package com.proconsi.electrobazar.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Temporary migration runner to clean up legacy columns after multilingual refactoring.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataMigrationRunner implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        log.info("Checking for legacy columns in products table...");
        String[] productLegacy = {"name", "description", "status", "low_stock_message"};
        for (String col : productLegacy) {
            try {
                jdbcTemplate.execute("ALTER TABLE products MODIFY COLUMN " + col + " VARCHAR(255) NULL");
            } catch (Exception e) {
                log.debug("Col " + col + " issue: " + e.getMessage());
            }
        }

        log.info("Checking for legacy columns in categories table...");
        String[] categoryLegacy = {"name", "description"};
        for (String col : categoryLegacy) {
            try {
                jdbcTemplate.execute("ALTER TABLE categories MODIFY COLUMN " + col + " VARCHAR(255) NULL");
            } catch (Exception e) {
                log.debug("Col " + col + " issue: " + e.getMessage());
            }
        }

        log.info("Ensuring wildcard product support (nullable product_id)...");
        try {
            // Force product_id to be nullable in both sale and suspended sale lines
            jdbcTemplate.execute("ALTER TABLE sale_lines MODIFY COLUMN product_id BIGINT NULL");
            jdbcTemplate.execute("ALTER TABLE suspended_sale_lines MODIFY COLUMN product_id BIGINT NULL");
            log.info("Successfully ensured product_id is nullable for wildcard products.");
        } catch (Exception e) {
            log.warn("Could not modify product_id columns (might already be NULL): " + e.getMessage());
        }

        log.info("Fixing product price precision in database...");
        try {
            jdbcTemplate.execute("ALTER TABLE products MODIFY COLUMN price DECIMAL(12,4) NOT NULL");
            jdbcTemplate.execute("ALTER TABLE products MODIFY COLUMN base_price_net DECIMAL(12,4) NOT NULL");
        } catch (Exception e) {
            log.debug("Price precision fix issue: " + e.getMessage());
        }

        log.info("Legacy column cleanup finished.");
    }
}
