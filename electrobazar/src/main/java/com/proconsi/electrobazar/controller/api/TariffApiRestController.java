package com.proconsi.electrobazar.controller.api;

import com.proconsi.electrobazar.model.Tariff;
import com.proconsi.electrobazar.service.TariffService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tariffs")
@RequiredArgsConstructor
public class TariffApiRestController {

    private final TariffService tariffService;

    /** GET /api/tariffs — Returns all active tariffs. */
    @GetMapping
    public ResponseEntity<List<Tariff>> getAll(
            @RequestParam(required = false, defaultValue = "false") boolean includeInactive) {
        List<Tariff> tariffs = includeInactive ? tariffService.findAll() : tariffService.findAllActive();
        return ResponseEntity.ok(tariffs);
    }

    /** GET /api/tariffs/{id} — Returns a tariff by ID. */
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return tariffService.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** GET /api/tariffs/customer-counts — Customer count per tariff. */
    @GetMapping("/customer-counts")
    public ResponseEntity<Map<Long, Long>> getCustomerCounts() {
        return ResponseEntity.ok(tariffService.getCustomerCountPerTariff());
    }

    /**
     * POST /api/tariffs — Creates a new custom tariff.
     * Body: { "name": "VIP", "discountPercentage": 20, "description": "..." }
     */
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        try {
            String name = (String) body.get("name");
            BigDecimal discount = new BigDecimal(body.get("discountPercentage").toString());
            String description = (String) body.getOrDefault("description", "");
            Tariff created = tariffService.create(name, discount, description);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al crear tarifa: " + e.getMessage()));
        }
    }

    /**
     * PUT /api/tariffs/{id} — Updates discount % and description of a tariff.
     * Body: { "discountPercentage": 12, "description": "..." }
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            BigDecimal discount = new BigDecimal(body.get("discountPercentage").toString());
            String description = (String) body.getOrDefault("description", "");
            Tariff updated = tariffService.update(id, discount, description);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al actualizar tarifa: " + e.getMessage()));
        }
    }

    /** DELETE /api/tariffs/{id}/deactivate — Deactivates a custom tariff. */
    @DeleteMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivate(@PathVariable Long id) {
        try {
            tariffService.deactivate(id);
            return ResponseEntity.ok(Map.of("message", "Tarifa desactivada correctamente."));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al desactivar tarifa: " + e.getMessage()));
        }
    }

    /** POST /api/tariffs/{id}/activate — Re-activates a custom tariff. */
    @PostMapping("/{id}/activate")
    public ResponseEntity<?> activate(@PathVariable Long id) {
        try {
            tariffService.activate(id);
            return ResponseEntity.ok(Map.of("message", "Tarifa activada correctamente."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al activar tarifa: " + e.getMessage()));
        }
    }
}
