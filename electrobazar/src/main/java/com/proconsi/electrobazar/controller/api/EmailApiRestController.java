package com.proconsi.electrobazar.controller.api;

import com.proconsi.electrobazar.model.Sale;
import com.proconsi.electrobazar.service.SaleService;
import com.proconsi.electrobazar.service.impl.AsyncEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@Slf4j
@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
public class EmailApiRestController {

    private final SaleService saleService;
    private final AsyncEmailService asyncEmailService;

    @PostMapping("/send-sale/{saleId}")
    public ResponseEntity<?> sendSaleEmail(@PathVariable Long saleId, @RequestParam String email) {
        log.info("Request to send sale email for Sale ID {} to {}", saleId, email);

        Sale sale = saleService.findById(saleId);
        if (sale == null) {
            return ResponseEntity.status(404).body(Collections.singletonMap("error", "Sale not found"));
        }

        // Dispatch to a separate Spring bean so @Async proxy takes effect
        asyncEmailService.sendSaleEmailAsync(sale, email);

        // Respond immediately — email is sent in the background
        return ResponseEntity.accepted()
                .body(Collections.singletonMap("message", "Email enviado en segundo plano"));
    }
}
