package com.proconsi.electrobazar.repository;

import com.proconsi.electrobazar.model.Tariff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TariffRepository extends JpaRepository<Tariff, Long> {

    Optional<Tariff> findByName(String name);

    List<Tariff> findByActiveTrueOrderByNameAsc();

    /** Count how many active customers are using each tariff. */
    @Query("SELECT t.id, COUNT(c) FROM Customer c JOIN c.tariff t WHERE c.active = true GROUP BY t.id")
    List<Object[]> countCustomersPerTariff();
}
