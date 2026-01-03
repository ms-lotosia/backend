package com.lotosia.identityservice.controller;

import com.lotosia.identityservice.dto.ResponseModel;
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

    @Operation(summary = "Get all users")
    @GetMapping("/users")
    public ResponseModel<Page<User>> getAllUsers(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);
        return ResponseModel.<Page<User>>builder()
                .data(users)
                .build();
    }

    @Operation(summary = "Get user by ID")
    @GetMapping("/users/{id}")
    public ResponseModel<User> getUserById(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseModel.<User>builder()
                .data(user)
                .build();
    }

    @Operation(summary = "Update user roles")
    @PutMapping("/users/{userId}/roles")
    public ResponseModel<User> updateUserRoles(@PathVariable Long userId, @RequestBody List<String> roleNames) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Role> roles = roleNames.stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new RuntimeException("Role not found: " + roleName)))
                .collect(Collectors.toList());

        user.getRoles().clear();
        user.getRoles().addAll(roles);
        userRepository.save(user);

        return ResponseModel.<User>builder()
                .data(user)
                .build();
    }

    @Operation(summary = "Create new role")
    @PostMapping("/roles")
    public ResponseModel<Role> createRole(@RequestParam String roleName) {
        if (roleRepository.findByName(roleName).isPresent()) {
            throw new RuntimeException("Role already exists: " + roleName);
        }

        Role role = new Role();
        role.setName(roleName);
        Role savedRole = roleRepository.save(role);

        return ResponseModel.<Role>builder()
                .data(savedRole)
                .build();
    }

    @Operation(summary = "Get all roles")
    @GetMapping("/roles")
    public ResponseModel<List<Role>> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        return ResponseModel.<List<Role>>builder()
                .data(roles)
                .build();
    }

    @Operation(summary = "Update role permissions")
    @PutMapping("/roles/{roleId}/permissions")
    public ResponseModel<Role> updateRolePermissions(@PathVariable Long roleId, @RequestBody List<String> permissionNames) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        List<Permission> permissions = permissionNames.stream()
                .map(permName -> permissionRepository.findByName(permName)
                        .orElseThrow(() -> new RuntimeException("Permission not found: " + permName)))
                .collect(Collectors.toList());

        role.getPermissions().clear();
        role.getPermissions().addAll(permissions);
        roleRepository.save(role);

        return ResponseModel.<Role>builder()
                .data(role)
                .build();
    }

    @Operation(summary = "Create new permission")
    @PostMapping("/permissions")
    public ResponseModel<Permission> createPermission(@RequestParam String permissionName) {
        if (permissionRepository.findByName(permissionName).isPresent()) {
            throw new RuntimeException("Permission already exists: " + permissionName);
        }

        Permission permission = new Permission();
        permission.setName(permissionName);
        Permission savedPermission = permissionRepository.save(permission);

        return ResponseModel.<Permission>builder()
                .data(savedPermission)
                .build();
    }

    @Operation(summary = "Get all permissions")
    @GetMapping("/permissions")
    public ResponseModel<List<Permission>> getAllPermissions() {
        List<Permission> permissions = permissionRepository.findAll();
        return ResponseModel.<List<Permission>>builder()
                .data(permissions)
                .build();
    }
}
