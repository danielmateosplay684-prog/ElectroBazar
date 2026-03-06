package com.proconsi.electrobazar.service;

import com.proconsi.electrobazar.model.AppSetting;
import com.proconsi.electrobazar.repository.AppSettingRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service responsible for validating and managing the operational admin PIN.
 *
 * <p>
 * The PIN is primarily stored in the database ({@code app_settings} table).
 * On startup, if no PIN is found in the database, it falls back to the
 * {@code ADMIN_PIN} environment variable (resolved through {@code admin.pin})
 * and seeds it into the database for future runtime updates.
 * </p>
 */
@Service
public class AdminPinService {

    private static final Logger log = LoggerFactory.getLogger(AdminPinService.class);
    private static final String PIN_KEY = "admin_pin";

    private final AppSettingRepository appSettingRepository;
    private final String fallbackPin;

    public AdminPinService(AppSettingRepository appSettingRepository,
            @Value("${admin.pin:}") String fallbackPin) {
        this.appSettingRepository = appSettingRepository;
        this.fallbackPin = fallbackPin;
    }

    /**
     * Fail-fast validation and initial seeding executed on application startup.
     *
     * <p>
     * If the PIN is missing from both the database and environment variables,
     * the application will refuse to start to avoid an insecure state.
     * </p>
     */
    @PostConstruct
    @Transactional
    public void validateAndSeedPin() {
        if (appSettingRepository.findByKey(PIN_KEY).isEmpty()) {
            if (fallbackPin == null || fallbackPin.isBlank()) {
                throw new IllegalStateException(
                        "[SECURITY] Fatal startup error: No admin PIN found in database and environment variable 'ADMIN_PIN' is missing.");
            }
            log.info("[SECURITY] Seeding admin PIN from environment variable fallback.");
            appSettingRepository.save(AppSetting.builder().key(PIN_KEY).value(fallbackPin).build());
        }
        log.info("[SECURITY] Admin PIN configuration validated successfully.");
    }

    /**
     * Retrieves the current admin PIN from the database.
     */
    private String getCurrentPin() {
        return appSettingRepository.findByKey(PIN_KEY)
                .map(AppSetting::getValue)
                .orElse(fallbackPin);
    }

    /**
     * Verifies whether the supplied PIN matches the configured admin PIN.
     *
     * @param pin the PIN attempt to verify; {@code null} is treated as invalid
     * @return {@code true} if the PIN matches, {@code false} otherwise
     */
    public boolean verifyPin(String pin) {
        if (pin == null || pin.isBlank()) {
            return false;
        }
        return getCurrentPin().equals(pin);
    }

    /**
     * Updates the admin PIN in the database after validating the current one.
     *
     * @param currentPin the current PIN for validation
     * @param newPin     the new PIN to set
     * @throws IllegalArgumentException if the current PIN is incorrect or new PIN
     *                                  is invalid
     */
    @Transactional
    public void updatePin(String currentPin, String newPin) {
        if (!verifyPin(currentPin)) {
            throw new IllegalArgumentException("El PIN actual es incorrecto.");
        }
        if (newPin == null || newPin.length() < 4) {
            throw new IllegalArgumentException("El nuevo PIN debe tener al menos 4 caracteres.");
        }

        AppSetting setting = appSettingRepository.findByKey(PIN_KEY)
                .orElse(AppSetting.builder().key(PIN_KEY).build());
        setting.setValue(newPin);
        appSettingRepository.save(setting);
        log.info("[SECURITY] Admin PIN updated successfully by an administrator.");
    }
}
