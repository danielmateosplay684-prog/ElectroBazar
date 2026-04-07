package com.proconsi.electrobazar.service;

import com.proconsi.electrobazar.model.MeasurementUnit;
import java.util.List;

public interface MeasurementUnitService {
    List<MeasurementUnit> findAll();
    MeasurementUnit findById(Long id);
    MeasurementUnit save(MeasurementUnit unit);
    void delete(Long id);
    MeasurementUnit findByName(String name);
}
