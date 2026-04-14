package com.proconsi.electrobazar.service.impl;

import com.proconsi.electrobazar.exception.ResourceNotFoundException;
import com.proconsi.electrobazar.model.CashRegister;
import com.proconsi.electrobazar.model.CashWithdrawal;
import com.proconsi.electrobazar.model.Worker;
import com.proconsi.electrobazar.repository.CashWithdrawalRepository;
import com.proconsi.electrobazar.service.ActivityLogService;
import com.proconsi.electrobazar.service.CashRegisterService;
import com.proconsi.electrobazar.service.CashWithdrawalService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CashWithdrawalServiceImpl implements CashWithdrawalService {

    private final CashWithdrawalRepository cashWithdrawalRepository;
    private final CashRegisterService cashRegisterService;
    private final ActivityLogService activityLogService;
    private final MessageSource messageSource;

    @Override
    public CashWithdrawal processMovement(Long registerId, BigDecimal amount, String reason,
            CashWithdrawal.MovementType type, Worker worker) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }

        // Pillamos la sesión (que es un CashRegister)
        CashRegister session = cashRegisterService.getOpenRegister()
                .orElseThrow(() -> new ResourceNotFoundException("No hay ninguna sesión de caja abierta."));

        if (type == CashWithdrawal.MovementType.WITHDRAWAL) {
            BigDecimal currentBalance = cashRegisterService.getCurrentCashBalance();
            if (amount.compareTo(currentBalance) > 0) {
                String localizedMsg = messageSource.getMessage("error.insufficient_cash", 
                    new Object[]{currentBalance}, LocaleContextHolder.getLocale());
                throw new com.proconsi.electrobazar.exception.InsufficientCashException(localizedMsg);
            }
        }

        if (Boolean.TRUE.equals(session.getClosed())) {
            throw new IllegalStateException("Cannot perform movements on a closed session.");
        }

        // Asignamos el objeto session entero
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
    public List<CashWithdrawal> findByRegisterId(Long registerId) {
        return cashWithdrawalRepository.findByCashRegisterId(registerId);
    }
}