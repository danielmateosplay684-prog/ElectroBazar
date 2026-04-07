package com.proconsi.electrobazar.controller.api;

import com.proconsi.electrobazar.model.MeasurementUnit;
import com.proconsi.electrobazar.service.MeasurementUnitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing Measurement Units.
 */
@RestController
@RequestMapping("/api/measurement-units")
@RequiredArgsConstructor
public class MeasurementUnitApiRestController {

    private final MeasurementUnitService measurementUnitService;

    /**
     * Retrieves all measurement units.
     * @return List of {@link MeasurementUnit} entities.
     */
    @GetMapping
    public ResponseEntity<List<MeasurementUnit>> getAll() {
        return ResponseEntity.ok(measurementUnitService.findAll());
    }

    /**
     * Retrieves a single measurement unit by ID.
     * @param id The measurement unit ID.
     * @return The requested {@link MeasurementUnit}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<MeasurementUnit> getById(@PathVariable Long id) {
        return ResponseEntity.ok(measurementUnitService.findById(id));
    }

    /**
     * Creates a new measurement unit.
     */
    @PostMapping
    public ResponseEntity<MeasurementUnit> create(@RequestBody MeasurementUnit unit) {
        return ResponseEntity.status(HttpStatus.CREATED).body(measurementUnitService.save(unit));
    }

    /**
     * Updates an existing measurement unit.
     */
    @PutMapping("/{id}")
    public ResponseEntity<MeasurementUnit> update(@PathVariable Long id, @RequestBody MeasurementUnit unit) {
        MeasurementUnit existing = measurementUnitService.findById(id);
        existing.setName(unit.getName());
        existing.setSymbol(unit.getSymbol());
        existing.setDecimalPlaces(unit.getDecimalPlaces());
        existing.setPromptOnAdd(unit.isPromptOnAdd());
        existing.setActive(unit.isActive());
        return ResponseEntity.ok(measurementUnitService.save(existing));
    }

    /**
     * Deletes a measurement unit.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        measurementUnitService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
