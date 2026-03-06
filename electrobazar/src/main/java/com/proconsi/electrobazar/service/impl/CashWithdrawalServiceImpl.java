package com.proconsi.electrobazar.service.impl;

import com.proconsi.electrobazar.exception.ResourceNotFoundException;
import com.proconsi.electrobazar.model.CashRegister;
import com.proconsi.electrobazar.model.CashWithdrawal;
import com.proconsi.electrobazar.model.Worker;
import com.proconsi.electrobazar.repository.CashRegisterRepository;
import com.proconsi.electrobazar.repository.CashWithdrawalRepository;
import com.proconsi.electrobazar.service.ActivityLogService;
import com.proconsi.electrobazar.service.CashWithdrawalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CashWithdrawalServiceImpl implements CashWithdrawalService {

    private final CashWithdrawalRepository cashWithdrawalRepository;
    private final CashRegisterRepository cashRegisterRepository;
    private final ActivityLogService activityLogService;

    @Override
    public CashWithdrawal processMovement(Long cashRegisterId, BigDecimal amount, String reason,
            CashWithdrawal.MovementType type, Worker worker) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Importe inválido");
        }

        CashRegister register = cashRegisterRepository.findById(cashRegisterId)
                .orElseThrow(() -> new ResourceNotFoundException("Caja no encontrada con id: " + cashRegisterId));

        if (register.getClosed()) {
            throw new IllegalStateException("No se puede realizar un movimiento en una caja ya cerrada");
        }

        CashWithdrawal movement = CashWithdrawal.builder()
                .cashRegister(register)
                .amount(amount)
                .reason(reason)
                .worker(worker)
                .type(type)
                .build();

        CashWithdrawal saved = cashWithdrawalRepository.save(movement);

        String typeStr = type == CashWithdrawal.MovementType.ENTRY ? "ENTRADA" : "RETIRADA";
        String username = worker != null ? worker.getUsername() : "Anónimo";

        activityLogService.logActivity(
                typeStr + "_CAJA",
                typeStr + " de caja de " + amount.setScale(2, java.math.RoundingMode.HALF_UP) + " \u20ac" +
                        (reason != null && !reason.isEmpty() ? ". Motivo: " + reason : ""),
                username,
                "CASH_REGISTER",
                register.getId());

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CashWithdrawal> findByRegisterId(Long registerId) {
        return cashWithdrawalRepository.findByCashRegisterId(registerId);
    }
}
