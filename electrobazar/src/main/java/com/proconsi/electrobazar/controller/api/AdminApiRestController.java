package com.proconsi.electrobazar.controller.api;

import com.proconsi.electrobazar.service.AdminPinService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminApiRestController {

    private final AdminPinService adminPinService;

    @PostMapping("/verify-pin")
    public ResponseEntity<?> verifyPin(@RequestBody Map<String, String> body) {
        String pin = body.get("pin");
        if (adminPinService.verifyPin(pin)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "PIN incorrecto"));
        }
    }
}
