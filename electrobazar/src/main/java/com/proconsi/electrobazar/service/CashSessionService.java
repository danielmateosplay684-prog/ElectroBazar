package com.proconsi.electrobazar.service;

import com.proconsi.electrobazar.model.CashRegister;
import com.proconsi.electrobazar.model.Worker;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CashSessionService {
    Optional<CashRegister> getActiveSession();
    List<CashRegister> findAllClosed();
    CashRegister findTodayIfClosed();
    CashRegister openSession(BigDecimal initialCash, Worker worker);
    CashRegister closeSession(BigDecimal actualCash, Worker worker);
    CashRegister findById(Long id);
}
