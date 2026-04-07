package com.proconsi.electrobazar.service.impl;

import com.proconsi.electrobazar.exception.ResourceNotFoundException;
import com.proconsi.electrobazar.model.MeasurementUnit;
import com.proconsi.electrobazar.repository.MeasurementUnitRepository;
import com.proconsi.electrobazar.service.MeasurementUnitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MeasurementUnitServiceImpl implements MeasurementUnitService {

    private final MeasurementUnitRepository measurementUnitRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MeasurementUnit> findAll() {
        return measurementUnitRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public MeasurementUnit findById(Long id) {
        return measurementUnitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unidad de medida no encontrada con id: " + id));
    }

    @Override
    public MeasurementUnit save(MeasurementUnit unit) {
        return measurementUnitRepository.save(unit);
    }

    @Override
    public void delete(Long id) {
        measurementUnitRepository.deleteById(id);
    }

    @Override
    public MeasurementUnit findByName(String name) {
        return measurementUnitRepository.findByName(name).orElse(null);
    }
}
