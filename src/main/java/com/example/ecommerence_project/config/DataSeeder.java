package com.example.ecommerence_project.config;

import com.example.ecommerence_project.entity.Role;
import com.example.ecommerence_project.entity.User;
import com.example.ecommerence_project.enums.RoleName;
import com.example.ecommerence_project.repository.RoleRepository;
import com.example.ecommerence_project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataSeeder {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner seedData() {
        return args -> {
            // ── Seed roles ────────────────────────────────────────────────────
            Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                    .orElseGet(() -> {
                        log.info("Creating ROLE_ADMIN");
                        return roleRepository.save(Role.builder().name(RoleName.ROLE_ADMIN).build());
                    });

            Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                    .orElseGet(() -> {
                        log.info("Creating ROLE_USER");
                        return roleRepository.save(Role.builder().name(RoleName.ROLE_USER).build());
                    });

            // ── Seed admin account ────────────────────────────────────────────
            if (!userRepository.existsByEmail("admin@perfume.com")) {
                User admin = User.builder()
                        .firstName("Admin")
                        .lastName("User")
                        .email("admin@perfume.com")
                        .password(passwordEncoder.encode("admin123"))
                        .roles(Set.of(adminRole, userRole))
                        .build();
                userRepository.save(admin);
                log.info("Seeded admin account: admin@perfume.com / admin123");
            }

            // ── Seed customer account ─────────────────────────────────────────
            if (!userRepository.existsByEmail("customer@perfume.com")) {
                User customer = User.builder()
                        .firstName("Test")
                        .lastName("Customer")
                        .email("customer@perfume.com")
                        .password(passwordEncoder.encode("customer123"))
                        .roles(Set.of(userRole))
                        .build();
                userRepository.save(customer);
                log.info("Seeded customer account: customer@perfume.com / customer123");
            }
        };
    }
}
