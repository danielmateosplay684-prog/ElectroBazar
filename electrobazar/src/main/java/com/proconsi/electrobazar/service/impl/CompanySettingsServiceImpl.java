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
        
        return repository.save(existing);
    }
}
