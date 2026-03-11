package com.proconsi.electrobazar.service;

import com.proconsi.electrobazar.model.CashRegister;
import com.proconsi.electrobazar.model.Tariff;
import com.proconsi.electrobazar.dto.TariffPriceEntryDTO;
import java.util.List;

public interface PdfReportService {
    /**
     * Generates a PDF report for the given closed cash register
     * and returns the bytes.
     *
     * @param register The closed CashRegister object
     * @return The PDF data as byte array
     */
    byte[] generateCashCloseReport(CashRegister register);

    /**
     * Generates a PDF report for the tariff price history.
     *
     * @param tariff The tariff
     * @param history The list of prices
     * @return The PDF data as byte array
     */
    byte[] generateTariffSheet(Tariff tariff, List<TariffPriceEntryDTO> history);
}
