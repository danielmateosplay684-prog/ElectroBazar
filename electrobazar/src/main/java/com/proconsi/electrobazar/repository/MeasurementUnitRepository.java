package com.proconsi.electrobazar.repository;

import com.proconsi.electrobazar.model.MeasurementUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MeasurementUnitRepository extends JpaRepository<MeasurementUnit, Long> {
    Optional<MeasurementUnit> findByName(String name);
}
