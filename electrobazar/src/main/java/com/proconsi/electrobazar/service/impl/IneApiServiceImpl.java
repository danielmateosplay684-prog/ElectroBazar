package com.proconsi.electrobazar.service.impl;

import com.proconsi.electrobazar.dto.IneIpcResponse;
import com.proconsi.electrobazar.service.IneApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Implementation of {@link IneApiService}.
 * Encapsulates communication with the Spanish INE (Instituto Nacional de
 * Estadística) API.
 * Includes a 24-hour manual cache to minimize external network calls.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IneApiServiceImpl implements IneApiService {

    private final RestTemplate restTemplate;
    private static final String INE_URL = "https://servicios.ine.es/wstempus/js/ES/DATOS_SERIE/IPC251856?nult=2";

    // Lightweight in-memory cache
    private BigDecimal cachedIpc;
    private LocalDateTime cacheExpiry;

    @Override
    public BigDecimal getLatestIpc() {
        if (cachedIpc != null && cacheExpiry != null && cacheExpiry.isAfter(LocalDateTime.now())) {
            log.debug("Returning cached IPC value: {}%", cachedIpc);
            return cachedIpc;
        }

        try {
            log.info(">>> [INE] Iniciando llamada a API del INE...");
            log.info(">>> [INE] URL: {}", INE_URL);

            // Primero obtén el raw JSON para ver qué devuelve exactamente
            String rawJson = restTemplate.getForObject(INE_URL, String.class);
            log.info(">>> [INE] Respuesta raw: {}", rawJson);

            if (rawJson == null || rawJson.isEmpty()) {
                log.error(">>> [INE] La respuesta fue NULL o vacía");
                return cachedIpc;
            }

            IneIpcResponse response = restTemplate.getForObject(INE_URL, IneIpcResponse.class);
            log.info(">>> [INE] Response parseado: {}", response);

            if (response == null) {
                log.error(">>> [INE] El parseo devolvió NULL");
                return cachedIpc;
            }

            if (response.getData() == null) {
                log.error(">>> [INE] response.getData() es NULL");
                return cachedIpc;
            }

            log.info(">>> [INE] Número de data points: {}", response.getData().size());
            response.getData().forEach(d -> log.info(">>> [INE] DataPoint: anyo={}, mes={}, valor={}", d.getAnyo(),
                    d.getMes(), d.getValor()));

            BigDecimal value = response.getData()
                    .stream()
                    .filter(d -> d.getValor() != null)
                    .reduce((first, second) -> second)
                    .map(IneIpcResponse.IneDataPoint::getValor)
                    .orElse(null);

            log.info(">>> [INE] Valor final extraído: {}", value);

            if (value != null) {
                this.cachedIpc = value;
                this.cacheExpiry = LocalDateTime.now().plusHours(24);
                return value;
            }

        } catch (Exception e) {
            log.error(">>> [INE] EXCEPCIÓN: tipo={}, mensaje={}", e.getClass().getName(), e.getMessage());
            log.error(">>> [INE] Stack trace completo:", e);
        }

        return cachedIpc;
    }
}
