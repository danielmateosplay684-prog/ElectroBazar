package com.proconsi.electrobazar.config;

import com.proconsi.electrobazar.model.Role;
import com.proconsi.electrobazar.model.Worker;
import com.proconsi.electrobazar.model.WorkerRepository;
import com.proconsi.electrobazar.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final WorkerRepository workerRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        // 1. Ensure the ADMIN role exists
        Role adminRole = roleRepository.findByName("ADMIN").orElseGet(() -> {
            Role newRole = Role.builder()
                    .name("ADMIN")
                    .description("Administrator with full access")
                    .permissions(Set.of("ADMIN_ACCESS"))
                    .build();
            return roleRepository.save(newRole);
        });

        // 2. Ensure the admin_final worker exists
        if (workerRepository.findByUsername("admin_final").isEmpty()) {
            Worker admin = Worker.builder()
                    .username("admin_final")
                    .password(passwordEncoder.encode("12345"))
                    .role(adminRole)
                    .active(true)
                    .build();
            workerRepository.save(admin);
            System.out.println("SAFE LOGIN CREATED: admin_final / 12345");
        } else {
            System.out.println("SAFE LOGIN SKIPPED: 'admin_final' already exists.");
        }
    }
}
