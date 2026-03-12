package com.proconsi.electrobazar.service.impl;

import com.proconsi.electrobazar.model.CompanySettings;
import com.proconsi.electrobazar.repository.CompanySettingsRepository;
import com.proconsi.electrobazar.service.CompanySettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompanySettingsServiceImpl implements CompanySettingsService {

    private final CompanySettingsRepository repository;
    private final com.proconsi.electrobazar.service.ActivityLogService activityLogService;

    @Override
    @Transactional(readOnly = true)
    public CompanySettings getSettings() {
        return repository.findById(1L).orElseGet(() -> CompanySettings.builder()
                .id(1L)
                .appName("ElectroBazar")
                .name("ElectroBazar S.L.")
                .cif("B12345678")
                .address("Calle Principal 123")
                .city("León")
                .postalCode("24001")
                .phone("987654321")
                .email("info@electrobazar.com")
                .website("www.electrobazar.com")
                .registroMercantil("Registro Mercantil de León, Tomo 1234, Folio 56, Hoja LE-7890")
                .invoiceFooterText("Gracias por su compra. Plazo de devolución: 15 días con ticket original.")
                .build());
    }

    @Override
    @Transactional
    public CompanySettings save(CompanySettings incoming) {
        CompanySettings existing = repository.findById(1L)
            .orElseThrow(() -> new RuntimeException("Company settings not found"));
        
        java.util.List<String> changes = new java.util.ArrayList<>();
        if (!java.util.Objects.equals(existing.getAppName(), incoming.getAppName())) changes.add("Nombre App");
        if (!java.util.Objects.equals(existing.getName(), incoming.getName())) changes.add("Empresa");
        if (!java.util.Objects.equals(existing.getCif(), incoming.getCif())) changes.add("CIF");
        if (!java.util.Objects.equals(existing.getAddress(), incoming.getAddress())) changes.add("Dirección");
        if (!java.util.Objects.equals(existing.getPhone(), incoming.getPhone())) changes.add("Teléfono");
        if (!java.util.Objects.equals(existing.getEmail(), incoming.getEmail())) changes.add("Email");

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
        
        CompanySettings saved = repository.save(existing);
        
        if (!changes.isEmpty()) {
            activityLogService.logActivity("ACTUALIZAR_CONFIGURACION", 
                "Configuración actualizada. Campos cambiados: " + String.join(", ", changes), 
                "Admin", "CONFIG", 1L);
        }
        
        return saved;
    }
}
