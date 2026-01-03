package com.lotosia.identityservice.service;

import com.lotosia.identityservice.entity.Role;
import com.lotosia.identityservice.entity.User;
import com.lotosia.identityservice.repository.RoleRepository;
import com.lotosia.identityservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.List;

@Component
@RequiredArgsConstructor
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
            List<User> allUsers = userRepository.findAll();

            boolean adminExists = allUsers.stream()
                    .anyMatch(user -> user.getRoles().stream()
                            .anyMatch(role -> "ADMIN".equals(role.getName())));

            if (!adminExists) {
                Role adminRole = roleRepository.findByName("ADMIN")
                        .orElseGet(() -> {
                            Role role = new Role();
                            role.setName("ADMIN");
                            return roleRepository.save(role);
                        });

                User adminUser = new User();
                adminUser.setEmail(adminEmail);
                adminUser.setPassword(passwordEncoder.encode(adminPassword));
                adminUser.setFirstName(adminFirstName);
                adminUser.setLastName(adminLastName);

                adminUser.getRoles().add(adminRole);
                adminRole.setUser(adminUser);

                userRepository.save(adminUser);
            }
        } catch (Exception e) {
            throw e;
        }
    }
}
