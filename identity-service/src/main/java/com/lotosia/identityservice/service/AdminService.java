package com.lotosia.identityservice.service;

import com.lotosia.identityservice.entity.Permission;
import com.lotosia.identityservice.entity.Role;
import com.lotosia.identityservice.entity.User;
import com.lotosia.identityservice.exception.AlreadyExistsException;
import com.lotosia.identityservice.repository.PermissionRepository;
import com.lotosia.identityservice.repository.RoleRepository;
import com.lotosia.identityservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;

    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User updateUserRoles(Long userId, List<String> roleNames) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Role> roles = roleNames.stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new RuntimeException("Role not found: " + roleName)))
                .collect(Collectors.toList());

        user.getRoles().clear();
        user.getRoles().addAll(roles);

        return userRepository.save(user);
    }

    public Role createRole(String roleName) {
        if (roleRepository.findByName(roleName).isPresent()) {
            throw new AlreadyExistsException("ROLE_ALREADY_EXISTS", "Role already exists: " + roleName);
        }

        Role role = new Role();
        role.setName(roleName);
        return roleRepository.save(role);
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public Role updateRolePermissions(Long roleId, List<String> permissionNames) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        List<Permission> permissions = permissionNames.stream()
                .map(permName -> permissionRepository.findByName(permName)
                        .orElseThrow(() -> new RuntimeException("Permission not found: " + permName)))
                .collect(Collectors.toList());

        role.getPermissions().clear();
        role.getPermissions().addAll(permissions);
        return roleRepository.save(role);
    }

    public Permission createPermission(String permissionName) {
        if (permissionRepository.findByName(permissionName).isPresent()) {
            throw new AlreadyExistsException("PERMISSION_ALREADY_EXISTS", "Permission already exists: " + permissionName);
        }

        Permission permission = new Permission();
        permission.setName(permissionName);
        return permissionRepository.save(permission);
    }

    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    public String createAdmin() {
        User existingAdmin = userRepository.findByEmail("admin@lotosia.com").orElse(null);

        if (existingAdmin != null) {
            return "EXISTS";
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

        userRepository.save(adminUser);
        return "CREATED";
    }
}
