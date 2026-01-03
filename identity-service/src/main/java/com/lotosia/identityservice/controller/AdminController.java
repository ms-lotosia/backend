package com.lotosia.identityservice.controller;

import com.lotosia.identityservice.entity.Permission;
import com.lotosia.identityservice.entity.Role;
import com.lotosia.identityservice.entity.User;
import com.lotosia.identityservice.repository.PermissionRepository;
import com.lotosia.identityservice.repository.RoleRepository;
import com.lotosia.identityservice.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Controller")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;

    @Operation(summary = "Get all users")
    @GetMapping("/users")
    public ResponseEntity<Page<User>> getAllUsers(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Get user by ID")
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Update user roles")
    @PutMapping("/users/{userId}/roles")
    public ResponseEntity<User> updateUserRoles(@PathVariable Long userId, @RequestBody List<String> roleNames) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Role> roles = roleNames.stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new RuntimeException("Role not found: " + roleName)))
                .collect(Collectors.toList());

        user.getRoles().forEach(role -> role.setUser(null));
        user.getRoles().clear();
        for (Role role : roles) {
            user.getRoles().add(role);
            role.setUser(user);
        }

        userRepository.save(user);

        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Create new role")
    @PostMapping("/roles")
    public ResponseEntity<Role> createRole(@RequestParam String roleName) {
        if (roleRepository.findByName(roleName).isPresent()) {
            throw new RuntimeException("Role already exists: " + roleName);
        }

        Role role = new Role();
        role.setName(roleName);
        Role savedRole = roleRepository.save(role);

        return ResponseEntity.ok(savedRole);
    }

    @Operation(summary = "Get all roles")
    @GetMapping("/roles")
    public ResponseEntity<List<Role>> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        return ResponseEntity.ok(roles);
    }

    @Operation(summary = "Update role permissions")
    @PutMapping("/roles/{roleId}/permissions")
    public ResponseEntity<Role> updateRolePermissions(@PathVariable Long roleId, @RequestBody List<String> permissionNames) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        List<Permission> permissions = permissionNames.stream()
                .map(permName -> permissionRepository.findByName(permName)
                        .orElseThrow(() -> new RuntimeException("Permission not found: " + permName)))
                .collect(Collectors.toList());

        role.getPermissions().clear();
        role.getPermissions().addAll(permissions);
        roleRepository.save(role);

        return ResponseEntity.ok(role);
    }

    @Operation(summary = "Create new permission")
    @PostMapping("/permissions")
    public ResponseEntity<Permission> createPermission(@RequestParam String permissionName) {
        if (permissionRepository.findByName(permissionName).isPresent()) {
            throw new RuntimeException("Permission already exists: " + permissionName);
        }

        Permission permission = new Permission();
        permission.setName(permissionName);
        Permission savedPermission = permissionRepository.save(permission);

        return ResponseEntity.ok(savedPermission);
    }

    @Operation(summary = "Get all permissions")
    @GetMapping("/permissions")
    public ResponseEntity<List<Permission>> getAllPermissions() {
        List<Permission> permissions = permissionRepository.findAll();
        return ResponseEntity.ok(permissions);
    }

    @Operation(summary = "Create the default admin user")
    @PostMapping("/create-admin")
    public ResponseEntity<User> createAdmin() {
        boolean adminExists = userRepository.findAll().stream()
                .anyMatch(user -> user.getRoles().stream()
                        .anyMatch(role -> "ADMIN".equals(role.getName())));

        if (adminExists) {
            throw new RuntimeException("Admin user already exists");
        }

        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName("ADMIN");
                    return roleRepository.save(role);
                });

        User adminUser = new User();
        adminUser.setEmail("admin@lotosia.com");
        adminUser.setPassword(passwordEncoder.encode("admin123!"));
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");

        adminUser.getRoles().add(adminRole);
        adminRole.setUser(adminUser);

        User savedUser = userRepository.save(adminUser);
        return ResponseEntity.ok(savedUser);
    }
}
