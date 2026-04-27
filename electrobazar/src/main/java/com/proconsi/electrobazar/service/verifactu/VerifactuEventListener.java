package com.proconsi.electrobazar.service.verifactu;

import com.proconsi.electrobazar.model.event.VerifactuSubmissionEvent;
import com.proconsi.electrobazar.service.VerifactuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class VerifactuEventListener {

    private final VerifactuService verifactuService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleVerifactuSubmission(VerifactuSubmissionEvent event) {
        log.debug("Verifactu: processing event {} for ID {} after commit", event.getType(), event.getId());
        
        switch (event.getType()) {
            case INVOICE -> verifactuService.submitInvoiceAsync(event.getId());
            case TICKET -> verifactuService.submitTicketAsync(event.getId());
            case RECTIFICATIVE -> verifactuService.submitRectificativeAsync(event.getId());
            case ANNULACION_INVOICE -> verifactuService.submitAnulacionAsync(event.getId(), false);
            case ANNULACION_TICKET -> verifactuService.submitAnulacionAsync(event.getId(), true);
        }
    }
}
