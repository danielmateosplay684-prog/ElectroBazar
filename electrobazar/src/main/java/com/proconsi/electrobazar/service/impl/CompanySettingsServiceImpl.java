package com.proconsi.electrobazar.service.impl;

import com.proconsi.electrobazar.model.CompanySettings;
import com.proconsi.electrobazar.repository.CompanySettingsRepository;
import com.proconsi.electrobazar.service.ActivityLogService;
import com.proconsi.electrobazar.service.CompanySettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Implementation of {@link CompanySettingsService}.
 * Focuses on maintaining the singleton-like company configuration entity (ID=1).
 */
@Service
@RequiredArgsConstructor
public class CompanySettingsServiceImpl implements CompanySettingsService {

    private final CompanySettingsRepository repository;
    private final ActivityLogService activityLogService;

    @Override
    @Transactional(readOnly = true)
    public CompanySettings getSettings() {
        // ID 1 is the convention for the single configuration row
        return repository.findById(1L).orElseGet(() -> CompanySettings.builder()
                .id(1L)
                .appName("")
                .name("")
                .cif("")
                .address("")
                .city("")
                .postalCode("")
                .phone("")
                .email("")
                .website("")
                .registroMercantil("")
                .invoiceFooterText("")
                .returnDeadlineDays(15)
                .build());
    }

    @Override
    @Transactional
    public CompanySettings save(CompanySettings incoming) {
        CompanySettings existing = repository.findById(1L)
                .orElse(CompanySettings.builder().id(1L).build());

        List<String> changes = new ArrayList<>();
        if (!Objects.equals(existing.getAppName(), incoming.getAppName())) changes.add("App Name");
        if (!Objects.equals(existing.getName(), incoming.getName())) changes.add("Company Name");
        if (!Objects.equals(existing.getCif(), incoming.getCif())) changes.add("CIF/VAT ID");
        if (!Objects.equals(existing.getAddress(), incoming.getAddress())) changes.add("Address");
        if (!Objects.equals(existing.getPhone(), incoming.getPhone())) changes.add("Phone");
        if (!Objects.equals(existing.getEmail(), incoming.getEmail())) changes.add("Email");

        // Map all fields from incoming to existing to preserve the ID
        existing.setAppName(incoming.getAppName());
        existing.setName(incoming.getName());
        existing.setCif(incoming.getCif());
        existing.setAddress(incoming.getAddress());
        existing.setCity(incoming.getCity());
        existing.setPostalCode(incoming.getPostalCode());
        existing.setPhone(incoming.getPhone());
        existing.setEmail(incoming.getEmail());
        existing.setWebsite(incoming.getWebsite());
        existing.setRegistroMercantil(incoming.getRegistroMercantil());
        existing.setInvoiceFooterText(incoming.getInvoiceFooterText());
        existing.setReturnDeadlineDays(incoming.getReturnDeadlineDays());

        CompanySettings saved = repository.save(existing);

        if (!changes.isEmpty()) {
            activityLogService.logActivity(
                    "ACTUALIZAR_CONFIGURACION",
                    "Company settings updated. Fields changed: " + String.join(", ", changes),
                    "Admin",
                    "CONFIG",
                    1L);
        }

        return saved;
    }
}


