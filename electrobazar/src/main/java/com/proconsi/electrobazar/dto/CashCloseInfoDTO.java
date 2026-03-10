package com.proconsi.electrobazar.dto;

import com.proconsi.electrobazar.model.CashRegister;
import com.proconsi.electrobazar.model.SaleReturn;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class CashCloseInfoDTO {
    private BigDecimal totalToday;
    private long countToday;
    private BigDecimal cardSalesToday;
    private BigDecimal cardRefundsToday;
    private BigDecimal cashSalesToday;
    private BigDecimal cashRefundsToday;
    private BigDecimal totalEntries;
    private BigDecimal totalWithdrawals;
    private BigDecimal expectedCashInDrawer;
    private long cancelledCount;
    private BigDecimal cancelledTotal;
    private List<SaleReturn> returnsToday;
    private BigDecimal openingBalance;
    private CashRegister todayRegister;
}
