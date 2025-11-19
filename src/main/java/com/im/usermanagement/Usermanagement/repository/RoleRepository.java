package com.im.usermanagement.repository;

import com.im.usermanagement.model.Role;
import com.im.usermanagement.model.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    // ⭐ NEW: Custom method to find a Role by its name (e.g., "ROLE_USER")
    Optional<Role> findByName(RoleName name);
}