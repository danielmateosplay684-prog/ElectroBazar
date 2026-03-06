package com.proconsi.electrobazar.service;

import com.proconsi.electrobazar.model.CashWithdrawal;
import com.proconsi.electrobazar.model.Worker;
import java.math.BigDecimal;
import java.util.List;

public interface CashWithdrawalService {
    CashWithdrawal processMovement(Long cashRegisterId, BigDecimal amount, String reason,
            CashWithdrawal.MovementType type, Worker worker);

    List<CashWithdrawal> findByRegisterId(Long registerId);
}
