package com.lotosia.identityservice.service;

import com.lotosia.identityservice.entity.Role;
import com.lotosia.identityservice.entity.User;
import com.lotosia.identityservice.repository.RoleRepository;
import com.lotosia.identityservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminUserInitializer {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:admin@lotosia.com}")
    private String adminEmail;

    @Value("${app.admin.password:admin123}")
    private String adminPassword;

    @Value("${app.admin.firstname:Admin}")
    private String adminFirstName;

    @Value("${app.admin.lastname:User}")
    private String adminLastName;

    @PostConstruct
    public void initializeAdminUser() {
        try {
            // Check if any admin users exist
            boolean adminExists = userRepository.findAll().stream()
                    .anyMatch(user -> user.getRoles().stream()
                            .anyMatch(role -> "ADMIN".equals(role.getName())));

            if (!adminExists) {
                log.info("No admin user found. Creating default admin user...");

                // Create ADMIN role if it doesn't exist
                Role adminRole = roleRepository.findByName("ADMIN")
                        .orElseGet(() -> {
                            Role role = new Role();
                            role.setName("ADMIN");
                            return roleRepository.save(role);
                        });

                // Create default admin user
                User adminUser = new User();
                adminUser.setEmail(adminEmail);
                adminUser.setPassword(passwordEncoder.encode(adminPassword));
                adminUser.setFirstName(adminFirstName);
                adminUser.setLastName(adminLastName);
                adminUser.getRoles().add(adminRole);

                User savedUser = userRepository.save(adminUser);

                log.info("Default admin user created successfully:");
                log.info("Email: {}", adminEmail);
                log.info("Password: {}", adminPassword);
                log.info("Please change the default password after first login!");
            } else {
                log.info("Admin user already exists. Skipping default admin creation.");
            }
        } catch (Exception e) {
            log.error("Error creating default admin user", e);
        }
    }
}
