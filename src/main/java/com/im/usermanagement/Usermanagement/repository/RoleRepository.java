package com.im.usermanagement.repository;

import com.im.usermanagement.model.Role;
import com.im.usermanagement.model.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Finds a Role entity by its RoleName enum value.
     * This method is essential for assigning roles during user registration.
     */
    Optional<Role> findByName(RoleName name);
}