package com.lotosia.identityservice.util;

import com.lotosia.identityservice.exception.PermissionNotFoundException;
import com.lotosia.identityservice.exception.RoleNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.function.Function;

@RequiredArgsConstructor
public class EntityFinder<T> {

    private final JpaRepository<T, Long> repository;
    private final Function<String, Optional<T>> findByNameFunction;
    private final String entityName;

    public T findByIdentifier(String identifier) {
        try {
            Long id = Long.parseLong(identifier);
            return repository.findById(id)
                    .orElseThrow(() -> createNotFoundException("ID: " + id));
        } catch (NumberFormatException e) {
            String normalizedName = identifier.trim().toUpperCase();
            return findByNameFunction.apply(normalizedName)
                    .orElseThrow(() -> createNotFoundException("name: " + identifier));
        }
    }

    private RuntimeException createNotFoundException(String identifier) {
        if ("Role".equals(entityName)) {
            return new RoleNotFoundException(entityName + " not found with " + identifier);
        } else if ("Permission".equals(entityName)) {
            return new PermissionNotFoundException(entityName + " not found with " + identifier);
        }
        return new RuntimeException(entityName + " not found with " + identifier);
    }

    public static <T> EntityFinder<T> roleFinder(JpaRepository<T, Long> repository, Function<String, Optional<T>> findByNameFunction) {
        return new EntityFinder<>(repository, findByNameFunction, "Role");
    }

    public static <T> EntityFinder<T> permissionFinder(JpaRepository<T, Long> repository, Function<String, Optional<T>> findByNameFunction) {
        return new EntityFinder<>(repository, findByNameFunction, "Permission");
    }
}
