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
        return taxRateRepository.save(taxRate);
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
            return ResponseEntity.ok(taxRateRepository.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        taxRateRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
