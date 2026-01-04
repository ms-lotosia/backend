package com.lotosia.identityservice.controller;

import com.lotosia.identityservice.dto.admin.AdminBootstrapResponse;
import com.lotosia.identityservice.dto.admin.PageResponse;
import com.lotosia.identityservice.dto.admin.PermissionDto;
import com.lotosia.identityservice.dto.admin.RoleDto;
import com.lotosia.identityservice.dto.admin.UserDto;
import com.lotosia.identityservice.entity.Permission;
import com.lotosia.identityservice.entity.Role;
import com.lotosia.identityservice.entity.User;
import com.lotosia.identityservice.exception.ApiError;
import com.lotosia.identityservice.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.api.annotations.ParameterObject;
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
            @ParameterObject
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

    @Operation(summary = "Update user roles",
               description = "Update user roles by providing role IDs or normalized names. " +
                           "IDs are preferred for precision, names are case-insensitive and trimmed. " +
                           "Duplicates are automatically ignored.")
    @PutMapping("/users/{userId}/roles")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UserDto> updateUserRoles(@PathVariable Long userId, @RequestBody Set<String> roleIdentifiers) {
        UserDto user = adminService.updateUserRoles(userId, new ArrayList<>(roleIdentifiers));
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
