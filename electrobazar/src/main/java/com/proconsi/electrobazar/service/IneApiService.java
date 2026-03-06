package com.proconsi.electrobazar.service;

import java.math.BigDecimal;

public interface IneApiService {
    /**
     * Fetches the latest annual IPC variation from INE.
     * 
     * @return the latest IPC value as a percentage (e.g. 2.3), or null if the API
     *         fails.
     */
    BigDecimal getLatestIpc();
}
