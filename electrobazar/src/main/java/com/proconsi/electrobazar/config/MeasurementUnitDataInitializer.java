package com.proconsi.electrobazar.config;

import com.proconsi.electrobazar.model.MeasurementUnit;
import com.proconsi.electrobazar.service.MeasurementUnitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



/**
 * Seeds initial measurement units if they don't exist.
 */
@Slf4j
@Configuration
public class MeasurementUnitDataInitializer {

    @Bean
    CommandLineRunner initUnitsData(MeasurementUnitService measurementUnitService) {
        return args -> {
            log.info("Initializing measurement units data...");
            createIfNotExist(measurementUnitService, "Unidad", "ud.", 0, false);
            createIfNotExist(measurementUnitService, "Kilogramo", "kg", 3, true);
            createIfNotExist(measurementUnitService, "Litro", "L", 3, true);
            createIfNotExist(measurementUnitService, "Metro", "m", 2, true);
        };
    }

    private void createIfNotExist(MeasurementUnitService svc, String name, String symbol, int decimals, boolean prompt) {
        if (svc.findByName(name) == null) {
            svc.save(MeasurementUnit.builder()
                    .name(name)
                    .symbol(symbol)
                    .decimalPlaces(decimals)
                    .promptOnAdd(prompt)
                    .active(true)
                    .build());
            log.info("Measurement unit created: {}", name);
        }
    }
}
