package com.lotosia.identityservice.controller;

import com.lotosia.identityservice.dto.admin.AdminBootstrapResponse;
import com.lotosia.identityservice.dto.admin.PageResponse;
import com.lotosia.identityservice.dto.admin.PermissionDto;
import com.lotosia.identityservice.dto.admin.RoleDto;
import com.lotosia.identityservice.dto.admin.UserDto;
import com.lotosia.identityservice.service.admin.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Controller")
public class AdminController {

    private final AdminService adminService;

    @Operation(summary = "Get all users with pagination")
    @GetMapping("/users")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<PageResponse<UserDto>> getAllUsers(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        PageResponse<UserDto> users = adminService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Get user by ID")
    @GetMapping("/users/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        UserDto user = adminService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Update user role",
               description = "Update user role by providing role ID or normalized name. " +
                           "IDs are preferred for precision, names are case-insensitive and trimmed.")
    @PutMapping("/users/{userId}/role")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UserDto> updateUserRole(@PathVariable Long userId, @RequestBody String roleIdentifier) {
        UserDto user = adminService.updateUserRole(userId, roleIdentifier);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Create new role")
    @PostMapping("/roles")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<RoleDto> createRole(@RequestParam String roleName) {
        RoleDto role = adminService.createRole(roleName);
        return ResponseEntity.ok(role);
    }

    @Operation(summary = "Get all roles")
    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<RoleDto>> getAllRoles() {
        List<RoleDto> roles = adminService.getAllRoles();
        return ResponseEntity.ok(roles);
    }

    @Operation(summary = "Get roles by status",
               description = "Filter roles by their enabled/disabled status")
    @GetMapping("/roles/status/{enabled}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<RoleDto>> getRolesByStatus(@PathVariable boolean enabled) {
        List<RoleDto> roles = adminService.getRolesByStatus(enabled);
        return ResponseEntity.ok(roles);
    }

    @Operation(summary = "Disable or enable a role",
               description = "Disable a role to prevent new assignments while keeping existing user assignments intact. " +
                           "Enable a previously disabled role.")
    @PutMapping("/roles/{roleId}/status")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<RoleDto> toggleRoleStatus(@PathVariable Long roleId, @RequestParam boolean enabled) {
        RoleDto role = adminService.toggleRoleStatus(roleId, enabled);
        return ResponseEntity.ok(role);
    }

    @Operation(summary = "Update role permissions",
               description = "Update role permissions by providing permission IDs or normalized names. " +
                           "IDs are preferred for precision, names are case-insensitive and trimmed. " +
                           "Duplicates are automatically ignored.")
    @PutMapping("/roles/{roleId}/permissions")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<RoleDto> updateRolePermissions(@PathVariable Long roleId, @RequestBody Set<String> permissionIdentifiers) {
        RoleDto role = adminService.updateRolePermissions(roleId, new ArrayList<>(permissionIdentifiers));
        return ResponseEntity.ok(role);
    }

    @Operation(summary = "Create new permission")
    @PostMapping("/permissions")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<PermissionDto> createPermission(@RequestParam String permissionName) {
        PermissionDto permission = adminService.createPermission(permissionName);
        return ResponseEntity.ok(permission);
    }

    @Operation(summary = "Get all permissions")
    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<PermissionDto>> getAllPermissions() {
        List<PermissionDto> permissions = adminService.getAllPermissions();
        return ResponseEntity.ok(permissions);
    }

    @Operation(summary = "Create or upgrade the default admin user")
    @PostMapping("/create-admin")
    @PreAuthorize("permitAll()")
    public ResponseEntity<AdminBootstrapResponse> createAdmin() {
        AdminBootstrapResponse response = adminService.createDefaultAdmin();
        return ResponseEntity.status(201).body(response);
    }
}
