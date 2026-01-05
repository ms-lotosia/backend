package com.lotosia.identityservice.service;

import com.lotosia.identityservice.dto.admin.AdminBootstrapResponse;
import com.lotosia.identityservice.dto.admin.PageResponse;
import com.lotosia.identityservice.dto.admin.PermissionDto;
import com.lotosia.identityservice.dto.admin.RoleDto;
import com.lotosia.identityservice.dto.admin.UserDto;
import com.lotosia.identityservice.entity.Permission;
import com.lotosia.identityservice.entity.Role;
import com.lotosia.identityservice.entity.User;
import com.lotosia.identityservice.exception.AdminAlreadyExistsException;
import com.lotosia.identityservice.exception.AdminUpgradeException;
import com.lotosia.identityservice.exception.AlreadyExistsException;
import com.lotosia.identityservice.exception.BadRequestException;
import com.lotosia.identityservice.exception.PermissionNotFoundException;
import com.lotosia.identityservice.exception.RoleNotFoundException;
import com.lotosia.identityservice.exception.UserNotFoundException;
import com.lotosia.identityservice.repository.PermissionRepository;
import com.lotosia.identityservice.repository.RoleRepository;
import com.lotosia.identityservice.repository.UserRepository;
import com.lotosia.identityservice.util.EntityFinder;
import jakarta.annotation.PostConstruct;
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

    private EntityFinder<Role> roleFinder;
    private EntityFinder<Permission> permissionFinder;

    @PostConstruct
    private void initializeFinders() {
        this.roleFinder = EntityFinder.roleFinder(
                roleRepository, roleRepository::findByName
        );
        this.permissionFinder = EntityFinder.permissionFinder(
                permissionRepository, permissionRepository::findByName
        );
    }

    private UserDto convertToUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole() != null ? user.getRole().getName() : null)
                .build();
    }

    private RoleDto convertToRoleDto(Role role) {
        return RoleDto.builder()
                .id(role.getId())
                .name(role.getName())
                .enabled(role.isEnabled())
                .permissions(role.getPermissions().stream()
                        .map(Permission::getName)
                        .collect(Collectors.toSet()))
                .build();
    }

    private PermissionDto convertToPermissionDto(Permission permission) {
        return PermissionDto.builder()
                .id(permission.getId())
                .name(permission.getName())
                .build();
    }

    private <T> PageResponse<T> convertToPageResponse(org.springframework.data.domain.Page<T> page) {
        PageResponse<T> response = new PageResponse<>();
        response.setContent(page.getContent());
        response.setPageNumber(page.getNumber());
        response.setPageSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setFirst(page.isFirst());
        response.setLast(page.isLast());
        response.setEmpty(page.isEmpty());
        return response;
    }

    public PageResponse<UserDto> getAllUsers(Pageable pageable) {
        org.springframework.data.domain.Page<User> userPage = userRepository.findAll(pageable);
        org.springframework.data.domain.Page<UserDto> userDtoPage = userPage.map(this::convertToUserDto);
        return convertToPageResponse(userDtoPage);
    }

    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
        return convertToUserDto(user);
    }

    public UserDto updateUserRole(Long userId, String roleIdentifier) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        Role role = roleFinder.findByIdentifier(roleIdentifier);
        user.setRole(role);

        User savedUser = userRepository.save(user);
        return convertToUserDto(savedUser);
    }


    public RoleDto createRole(String roleName) {
        if (roleRepository.findByName(roleName).isPresent()) {
            throw new AlreadyExistsException("ROLE_ALREADY_EXISTS", "Role already exists: " + roleName);
        }

        Role role = new Role();
        role.setName(roleName);
        Role savedRole = roleRepository.save(role);
        return convertToRoleDto(savedRole);
    }

    public List<RoleDto> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::convertToRoleDto)
                .collect(Collectors.toList());
    }

    public RoleDto updateRolePermissions(Long roleId, List<String> permissionIdentifiers) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException("Role not found with ID: " + roleId));

        List<Permission> permissions = permissionIdentifiers.stream()
                .map(permissionFinder::findByIdentifier)
                .collect(Collectors.toList());

        role.getPermissions().clear();
        role.getPermissions().addAll(permissions);
        Role savedRole = roleRepository.save(role);
        return convertToRoleDto(savedRole);
    }

    public RoleDto toggleRoleStatus(Long roleId, boolean enabled) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException("Role not found with ID: " + roleId));

        if (!enabled && ("ADMIN".equals(role.getName()) || "USER".equals(role.getName()))) {
            throw new BadRequestException("CANNOT_DISABLE_SYSTEM_ROLE",
                    "System roles (ADMIN, USER) cannot be disabled for system security");
        }

        role.setEnabled(enabled);
        Role savedRole = roleRepository.save(role);
        return convertToRoleDto(savedRole);
    }


    public PermissionDto createPermission(String permissionName) {
        if (permissionRepository.findByName(permissionName).isPresent()) {
            throw new AlreadyExistsException("PERMISSION_ALREADY_EXISTS", "Permission already exists: " + permissionName);
        }

        Permission permission = new Permission();
        permission.setName(permissionName);
        Permission savedPermission = permissionRepository.save(permission);
        return convertToPermissionDto(savedPermission);
    }

    public List<PermissionDto> getAllPermissions() {
        return permissionRepository.findAll().stream()
                .map(this::convertToPermissionDto)
                .collect(Collectors.toList());
    }

    public AdminBootstrapResponse createDefaultAdmin() {
        User existingAdmin = userRepository.findByEmail("admin@lotosia.com").orElse(null);

        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName("ADMIN");
                    return roleRepository.save(role);
                });

        if (existingAdmin != null) {
            boolean hadAdminRole = existingAdmin.getRole() != null && "ADMIN".equals(existingAdmin.getRole().getName());

            existingAdmin.setRole(adminRole);
            userRepository.save(existingAdmin);

            if (hadAdminRole) {
                AdminBootstrapResponse response = new AdminBootstrapResponse();
                response.setStatus(AdminBootstrapResponse.AdminBootstrapStatus.EXISTS);
                response.setMessage("Admin user already exists with admin privileges");
                throw new AdminAlreadyExistsException(response);
            } else {
                AdminBootstrapResponse response = new AdminBootstrapResponse();
                response.setStatus(AdminBootstrapResponse.AdminBootstrapStatus.UPGRADED);
                response.setMessage("Existing user upgraded to admin privileges");
                throw new AdminUpgradeException(response);
            }
        }

        User adminUser = new User();
        adminUser.setEmail("admin@lotosia.com");
        adminUser.setPassword(passwordEncoder.encode("admin123!"));
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");

        adminUser.setRole(adminRole);

        userRepository.save(adminUser);
        return AdminBootstrapResponse.builder()
                .status(AdminBootstrapResponse.AdminBootstrapStatus.CREATED)
                .message("Admin user created successfully")
                .build();
    }
}
