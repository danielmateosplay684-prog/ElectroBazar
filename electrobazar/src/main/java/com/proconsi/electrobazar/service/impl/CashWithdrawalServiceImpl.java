package com.proconsi.electrobazar.service.impl;

import com.proconsi.electrobazar.exception.ResourceNotFoundException;
import com.proconsi.electrobazar.model.CashRegister;
import com.proconsi.electrobazar.model.CashWithdrawal;
import com.proconsi.electrobazar.model.Worker;
import com.proconsi.electrobazar.repository.CashWithdrawalRepository;
import com.proconsi.electrobazar.service.ActivityLogService;
import com.proconsi.electrobazar.service.CashSessionService;
import com.proconsi.electrobazar.service.CashWithdrawalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Implementation of {@link CashWithdrawalService}.
 * Handles manual cash entries and withdrawals within an open shift.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CashWithdrawalServiceImpl implements CashWithdrawalService {

    private final CashWithdrawalRepository cashWithdrawalRepository;
    private final CashSessionService cashSessionService;
    private final ActivityLogService activityLogService;

    @Override
    public CashWithdrawal processMovement(Long sessionId, BigDecimal amount, String reason,
            CashWithdrawal.MovementType type, Worker worker) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }

        CashRegister session = cashSessionService.getActiveSession()
                .filter(s -> s.getId().equals(sessionId))
                .orElseThrow(() -> new ResourceNotFoundException("Active cash session not found with id: " + sessionId));

        if (Boolean.TRUE.equals(session.getClosed())) {
            throw new IllegalStateException("Cannot perform movements on a closed session.");
        }

        CashWithdrawal movement = CashWithdrawal.builder()
                .cashRegister(session)
                .amount(amount)
                .reason(reason)
                .worker(worker)
                .type(type)
                .build();

        CashWithdrawal saved = cashWithdrawalRepository.save(movement);

        String typeLabel = type == CashWithdrawal.MovementType.ENTRY ? "ENTRADA" : "RETIRADA";
        
        activityLogService.logActivity(
                "MOVIMIENTO_CAJA",
                String.format("%s de %.2f €. Motivo: %s", typeLabel, amount, (reason != null ? reason : "N/A")),
                "Admin",
                "CASH_WITHDRAWAL",
                saved.getId());

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CashWithdrawal> findBySessionId(Long sessionId) {
        return cashWithdrawalRepository.findByCashRegisterId(sessionId);
    }
}
