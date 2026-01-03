package com.lotosia.identityservice.controller;

import com.lotosia.identityservice.entity.Permission;
import com.lotosia.identityservice.entity.Role;
import com.lotosia.identityservice.entity.User;
import com.lotosia.identityservice.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Controller")
public class AdminController {

    private final AdminService adminService;

    @Operation(summary = "Get all users")
    @GetMapping("/users")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Page<User>> getAllUsers(Pageable pageable) {
        Page<User> users = adminService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Get user by ID")
    @GetMapping("/users/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = adminService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Update user roles")
    @PutMapping("/users/{userId}/roles")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<User> updateUserRoles(@PathVariable Long userId, @RequestBody List<String> roleNames) {
        User user = adminService.updateUserRoles(userId, roleNames);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Create new role")
    @PostMapping("/roles")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Role> createRole(@RequestParam String roleName) {
        Role role = adminService.createRole(roleName);
        return ResponseEntity.ok(role);
    }

    @Operation(summary = "Get all roles")
    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<Role>> getAllRoles() {
        List<Role> roles = adminService.getAllRoles();
        return ResponseEntity.ok(roles);
    }

    @Operation(summary = "Update role permissions")
    @PutMapping("/roles/{roleId}/permissions")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Role> updateRolePermissions(@PathVariable Long roleId, @RequestBody List<String> permissionNames) {
        Role role = adminService.updateRolePermissions(roleId, permissionNames);
        return ResponseEntity.ok(role);
    }

    @Operation(summary = "Create new permission")
    @PostMapping("/permissions")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Permission> createPermission(@RequestParam String permissionName) {
        Permission permission = adminService.createPermission(permissionName);
        return ResponseEntity.ok(permission);
    }

    @Operation(summary = "Get all permissions")
    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<Permission>> getAllPermissions() {
        List<Permission> permissions = adminService.getAllPermissions();
        return ResponseEntity.ok(permissions);
    }

    @Operation(summary = "Create the default admin user")
    @PostMapping("/create-admin")
    @PreAuthorize("permitAll()")
    public ResponseEntity<String> createAdmin() {
        String message = adminService.createAdmin();
        return ResponseEntity.ok(message);
    }
}
