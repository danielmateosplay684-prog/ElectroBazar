package com.proconsi.electrobazar.repository;

import com.proconsi.electrobazar.model.TaxRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaxRateRepository extends JpaRepository<TaxRate, Long> {
    List<TaxRate> findByActiveTrue();
    List<TaxRate> findByValidFromAfter(java.time.LocalDate date);
    List<TaxRate> findByDescriptionAndIdNot(String description, Long id);
    List<TaxRate> findByValidFromAndActiveTrue(java.time.LocalDate date);
}
