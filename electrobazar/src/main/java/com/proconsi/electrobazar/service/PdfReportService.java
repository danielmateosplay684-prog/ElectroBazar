package com.proconsi.electrobazar.service;

import com.proconsi.electrobazar.model.CashRegister;

public interface PdfReportService {
        /**
         * Generates a PDF report for the given closed cash register
         * and returns the bytes.
         *
         * @param register The closed CashRegister object
         * @return The PDF data as byte array
         */
        byte[] generateCashCloseReport(CashRegister register);
}
