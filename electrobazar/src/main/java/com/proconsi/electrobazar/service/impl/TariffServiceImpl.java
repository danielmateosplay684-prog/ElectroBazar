package com.proconsi.electrobazar.service.impl;

import com.proconsi.electrobazar.model.Customer;
import com.proconsi.electrobazar.model.Tariff;
import com.proconsi.electrobazar.repository.CustomerRepository;
import com.proconsi.electrobazar.repository.TariffRepository;
import com.proconsi.electrobazar.repository.ProductRepository;
import com.proconsi.electrobazar.repository.TariffPriceHistoryRepository;
import com.proconsi.electrobazar.util.RecargoEquivalenciaCalculator;
import com.proconsi.electrobazar.service.TariffService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TariffServiceImpl implements TariffService {

    private final TariffRepository tariffRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final TariffPriceHistoryRepository tariffPriceHistoryRepository;
    private final RecargoEquivalenciaCalculator recargoCalculator;

    @Override
    @Transactional(readOnly = true)
    public List<Tariff> findAll() {
        return tariffRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tariff> findAllActive() {
        return tariffRepository.findByActiveTrueOrderByNameAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Tariff> findById(Long id) {
        return tariffRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Tariff> findByName(String name) {
        return tariffRepository.findByName(name);
    }

    @Override
    @Transactional(readOnly = true)
    public Tariff getDefault() {
        return tariffRepository.findByName(Tariff.MINORISTA)
                .orElseThrow(() -> new IllegalStateException(
                        "System tariff MINORISTA not found – data initializer may have failed."));
    }

    @Override
    public Tariff create(String name, BigDecimal discountPercentage, String description) {
        if (tariffRepository.findByName(name.toUpperCase()).isPresent()) {
            throw new IllegalArgumentException("Ya existe una tarifa con el nombre: " + name);
        }
        Tariff tariff = Tariff.builder()
                .name(name.toUpperCase())
                .discountPercentage(discountPercentage != null ? discountPercentage : BigDecimal.ZERO)
                .description(description)
                .active(true)
                .systemTariff(false)
                .build();
        Tariff savedTariff = tariffRepository.save(tariff);

        // Auto-generate tariff history for all active products
        List<com.proconsi.electrobazar.model.Product> products = productRepository.findByActiveTrueOrderByNameAsc();
        List<com.proconsi.electrobazar.model.TariffPriceHistory> histories = new ArrayList<>();
        java.time.LocalDate today = java.time.LocalDate.now();

        BigDecimal discountPct = savedTariff.getDiscountPercentage();
        BigDecimal discountMultiplier = BigDecimal.ONE.subtract(discountPct.divide(BigDecimal.valueOf(100), 10, java.math.RoundingMode.HALF_UP));

        Map<BigDecimal, BigDecimal> vatToReRateMap = recargoCalculator.getVatToReRateMap();

        for (com.proconsi.electrobazar.model.Product product : products) {
            BigDecimal basePriceWithVat = product.getPrice();
            BigDecimal vatRate = (product.getTaxRate() != null && product.getTaxRate().getVatRate() != null) 
                    ? product.getTaxRate().getVatRate() : new BigDecimal("0.21");
            
            // net_price = (base_price_with_vat / (1 + vatRate)) * (1 - discountPercent/100)
            BigDecimal netPrice = basePriceWithVat.divide(BigDecimal.ONE.add(vatRate), 10, java.math.RoundingMode.HALF_UP)
                    .multiply(discountMultiplier).setScale(2, java.math.RoundingMode.HALF_UP);
            
            BigDecimal priceWithVat = netPrice.multiply(BigDecimal.ONE.add(vatRate)).setScale(2, java.math.RoundingMode.HALF_UP);
            
            BigDecimal normalizedVat = vatRate.stripTrailingZeros();
            BigDecimal reRate = vatToReRateMap.entrySet().stream()
                    .filter(entry -> entry.getKey().stripTrailingZeros().compareTo(normalizedVat) == 0)
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElse(BigDecimal.ZERO);
            
            BigDecimal priceWithRe = netPrice.multiply(BigDecimal.ONE.add(vatRate).add(reRate)).setScale(2, java.math.RoundingMode.HALF_UP);

            histories.add(com.proconsi.electrobazar.model.TariffPriceHistory.builder()
                    .product(product)
                    .tariff(savedTariff)
                    .basePrice(basePriceWithVat)
                    .netPrice(netPrice)
                    .vatRate(vatRate)
                    .priceWithVat(priceWithVat)
                    .reRate(reRate)
                    .priceWithRe(priceWithRe)
                    .discountPercent(discountPct)
                    .validFrom(today)
                    .validTo(null)
                    .build());
        }
        tariffPriceHistoryRepository.saveAll(histories);

        return savedTariff;
    }

    @Override
    public Tariff update(Long id, BigDecimal discountPercentage, String description) {
        Tariff tariff = tariffRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tarifa no encontrada con id: " + id));
        tariff.setDiscountPercentage(discountPercentage != null ? discountPercentage : BigDecimal.ZERO);
        tariff.setDescription(description);
        return tariffRepository.save(tariff);
    }

    @Override
    public void deactivate(Long id) {
        Tariff tariff = tariffRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tarifa no encontrada con id: " + id));
        if (Boolean.TRUE.equals(tariff.getSystemTariff())) {
            throw new IllegalStateException("No se puede desactivar una tarifa del sistema: " + tariff.getName());
        }
        tariff.setActive(false);
        tariffRepository.save(tariff);

        // Move all customers using this tariff back to MINORISTA
        Tariff minorista = getDefault();
        List<Customer> affected = customerRepository.findAll().stream()
                .filter(c -> tariff.equals(c.getTariff()))
                .toList();
        affected.forEach(c -> c.setTariff(minorista));
        customerRepository.saveAll(affected);
        log.info("Tariff '{}' deactivated. {} customers moved to MINORISTA.", tariff.getName(), affected.size());
    }

    @Override
    public void activate(Long id) {
        Tariff tariff = tariffRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tarifa no encontrada con id: " + id));
        tariff.setActive(true);
        tariffRepository.save(tariff);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Long> getCustomerCountPerTariff() {
        Map<Long, Long> result = new HashMap<>();
        tariffRepository.countCustomersPerTariff()
                .forEach(row -> result.put((Long) row[0], (Long) row[1]));
        return result;
    }
}
