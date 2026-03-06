package com.proconsi.electrobazar.service;

import com.proconsi.electrobazar.model.Tariff;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TariffService {

    /** Returns all tariffs (active and inactive). */
    List<Tariff> findAll();

    /** Returns only active tariffs, sorted by name. */
    List<Tariff> findAllActive();

    /** Looks up a tariff by its primary key. */
    Optional<Tariff> findById(Long id);

    /** Looks up a tariff by its unique name. */
    Optional<Tariff> findByName(String name);

    /**
     * Returns the MINORISTA tariff (always exists).
     * Used as the default when a customer has no tariff.
     */
    Tariff getDefault();

    /** Creates a new custom tariff. */
    Tariff create(String name, BigDecimal discountPercentage, String description);

    /** Updates the discount percentage and description of an existing tariff. */
    Tariff update(Long id, BigDecimal discountPercentage, String description);

    /**
     * Deactivates a custom tariff (system tariffs cannot be deactivated).
     * Customers using this tariff will be automatically moved to MINORISTA.
     */
    void deactivate(Long id);

    /**
     * Re-activates a previously deactivated custom tariff.
     */
    void activate(Long id);

    /**
     * Returns a map of tariffId → customerCount for all tariffs
     * (only active customers are counted).
     */
    Map<Long, Long> getCustomerCountPerTariff();
}
