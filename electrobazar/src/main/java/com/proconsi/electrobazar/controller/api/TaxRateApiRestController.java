package com.proconsi.electrobazar.controller.api;

import com.proconsi.electrobazar.model.TaxRate;
import com.proconsi.electrobazar.repository.TaxRateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tax-rates")
@RequiredArgsConstructor
public class TaxRateApiRestController {

    private final TaxRateRepository taxRateRepository;
    private final com.proconsi.electrobazar.service.ActivityLogService activityLogService;

    @GetMapping
    public List<TaxRate> getAll() {
        return taxRateRepository.findAll();
    }

    @GetMapping("/active")
    public List<TaxRate> getActive() {
        return taxRateRepository.findByActiveTrue();
    }

    @PostMapping
    public TaxRate create(@RequestBody TaxRate taxRate) {
        // Automatically set valid_to of current active TaxRate with same description
        if (taxRate.getValidFrom() != null) {
            taxRateRepository.findByActiveTrue().stream()
                .filter(tr -> tr.getDescription() != null && tr.getDescription().equals(taxRate.getDescription()))
                .forEach(tr -> {
                    tr.setValidTo(taxRate.getValidFrom().minusDays(1));
                    taxRateRepository.save(tr);
                });
        }
        TaxRate saved = taxRateRepository.save(taxRate);
        activityLogService.logActivity("CREAR_IVA", "Nuevo tipo de IVA creado: " + saved.getDescription() + " (" + saved.getVatRate().multiply(new java.math.BigDecimal("100")) + "%)", "Admin", "TAX_RATE", saved.getId());
        return saved;
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaxRate> update(@PathVariable Long id, @RequestBody TaxRate taxRate) {
        return taxRateRepository.findById(id).map(existing -> {
            existing.setVatRate(taxRate.getVatRate());
            existing.setReRate(taxRate.getReRate());
            existing.setDescription(taxRate.getDescription());
            existing.setActive(taxRate.getActive());
            existing.setValidFrom(taxRate.getValidFrom());
            existing.setValidTo(taxRate.getValidTo());

            // If we are updating validFrom, we might need to adjust the previous rate's validTo
            if (taxRate.getValidFrom() != null) {
                taxRateRepository.findByActiveTrue().stream()
                    .filter(tr -> !tr.getId().equals(id) && tr.getDescription() != null && tr.getDescription().equals(taxRate.getDescription()))
                    .forEach(tr -> {
                        tr.setValidTo(taxRate.getValidFrom().minusDays(1));
                        taxRateRepository.save(tr);
                    });
            }

            TaxRate saved = taxRateRepository.save(existing);
            activityLogService.logActivity("ACTUALIZAR_IVA", "Tipo de IVA actualizado: " + saved.getDescription() + " (" + saved.getVatRate().multiply(new java.math.BigDecimal("100")) + "%)", "Admin", "TAX_RATE", saved.getId());
            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        taxRateRepository.findById(id).ifPresent(tr -> {
            taxRateRepository.deleteById(id);
            activityLogService.logActivity("ELIMINAR_IVA", "Tipo de IVA eliminado: " + tr.getDescription(), "Admin", "TAX_RATE", id);
        });
        return ResponseEntity.ok().build();
    }
}
