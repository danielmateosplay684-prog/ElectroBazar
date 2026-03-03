package com.proconsi.electrobazar.controller.web;

import com.proconsi.electrobazar.model.Worker;
import com.proconsi.electrobazar.service.WorkerService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class LoginController {

    private final WorkerService workerService;

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, HttpSession session,
            Model model) {
        Optional<Worker> worker = workerService.login(username, password);
        if (worker.isPresent()) {
            Worker w = worker.get();
            session.setAttribute("worker", w);

            // Populate Spring Security Context for session-based auth
            java.util.List<org.springframework.security.core.authority.SimpleGrantedAuthority> authorities = w
                    .getEffectivePermissions()
                    .stream()
                    .map(org.springframework.security.core.authority.SimpleGrantedAuthority::new)
                    .collect(java.util.stream.Collectors.toList());

            org.springframework.security.authentication.UsernamePasswordAuthenticationToken auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                    w.getUsername(), null, authorities);

            org.springframework.security.core.context.SecurityContext context = org.springframework.security.core.context.SecurityContextHolder
                    .createEmptyContext();
            context.setAuthentication(auth);
            org.springframework.security.core.context.SecurityContextHolder.setContext(context);
            session.setAttribute(
                    org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    context);

            // Si tiene permiso de acceso admin, le damos la sesión de admin
            if (w.getEffectivePermissions().contains("ADMIN_ACCESS")) {
                session.setAttribute("admin", true);
            }
            return "redirect:/tpv";
        } else {
            model.addAttribute("error", "Usuario o contraseña incorrectos");
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
