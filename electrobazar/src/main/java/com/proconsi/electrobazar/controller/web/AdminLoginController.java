package com.proconsi.electrobazar.controller.web;

import com.proconsi.electrobazar.service.AdminPinService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminLoginController {

    private final AdminPinService adminPinService;

    public AdminLoginController(AdminPinService adminPinService) {
        this.adminPinService = adminPinService;
    }

    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<?> login(@RequestBody Map<String, String> body, HttpSession session) {
        String pin = body.getOrDefault("pin", "");
        if (adminPinService.verifyPin(pin)) {
            session.setAttribute("admin", true);

            // Escalate privileges in SecurityContext
            org.springframework.security.core.Authentication currentAuth = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication();
            java.util.List<org.springframework.security.core.authority.SimpleGrantedAuthority> authorities = new java.util.ArrayList<>();
            String username = "admin";

            if (currentAuth != null) {
                username = currentAuth.getName();
                authorities.addAll(currentAuth.getAuthorities().stream()
                        .map(a -> new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                a.getAuthority()))
                        .collect(java.util.stream.Collectors.toList()));
            }

            if (authorities.stream().noneMatch(a -> a.getAuthority().equals("ADMIN_ACCESS"))) {
                authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority("ADMIN_ACCESS"));
            }

            org.springframework.security.authentication.UsernamePasswordAuthenticationToken newAuth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                    username, null, authorities);

            org.springframework.security.core.context.SecurityContext context = org.springframework.security.core.context.SecurityContextHolder
                    .getContext();
            context.setAuthentication(newAuth);
            session.setAttribute(
                    org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    context);

            return ResponseEntity.ok(Map.of("ok", true));
        }
        return ResponseEntity.status(401).body(Map.of("ok", false, "message", "PIN incorrecto"));
    }

    @PostMapping("/change-pin")
    @ResponseBody
    public ResponseEntity<?> changePin(@RequestBody Map<String, String> body, HttpSession session) {
        if (!Boolean.TRUE.equals(session.getAttribute("admin"))) {
            return ResponseEntity.status(401).body(Map.of("ok", false, "message", "No autorizado"));
        }

        String oldPin = body.getOrDefault("oldPin", "");
        String newPin = body.getOrDefault("newPin", "");

        if (adminPinService.changePin(oldPin, newPin)) {
            return ResponseEntity.ok(Map.of("ok", true));
        }
        return ResponseEntity.status(400)
                .body(Map.of("ok", false, "message", "No se pudo cambiar el PIN verifique el PIN actual."));
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/tpv";
    }
}
