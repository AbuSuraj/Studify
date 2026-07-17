package com.edutech.studify.config;


import com.edutech.studify.entity.Role;
import com.edutech.studify.entity.User;
import com.edutech.studify.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Runs automatically on application startup.
 * Ensures the system always has at least one super-admin account.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username:superadmin}")
    private String adminUsername;

    @Value("${app.admin.email:admin@studify.com}")
    private String adminEmail;

    @Value("${app.admin.password:}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        seedSuperAdmin();
    }

    private void seedSuperAdmin() {
        if (userRepository.existsByRole(Role.ADMIN)) {
            log.info("ADMIN account already exists. Skipping bootstrap.");
            return;
        }

        if (!StringUtils.hasText(adminPassword)) {
            log.warn("No ADMIN account found and ADMIN_PASSWORD is not set. " +
                    "Skipping admin bootstrap. Set the ADMIN_PASSWORD environment " +
                    "variable and restart to create the first admin.");
            return;
        }

        if (userRepository.existsByUsername(adminUsername) || userRepository.existsByEmail(adminEmail)) {
            log.error("Cannot bootstrap admin: username '{}' or email '{}' is already taken. " +
                    "Set a different ADMIN_USERNAME/ADMIN_EMAIL.", adminUsername, adminEmail);
            return;
        }

        log.info("No ADMIN account found. Bootstrapping default super-admin...");

        User superAdmin = User.builder()
                .username(adminUsername)
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .role(Role.ADMIN)
                .isActive(true)
                .build();

        userRepository.save(superAdmin);

        log.warn("Super-admin created successfully! Email: {}. " +
                "Log in and change this password immediately.", adminEmail);
    }
}