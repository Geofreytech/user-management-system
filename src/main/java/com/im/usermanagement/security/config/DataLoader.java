package com.im.usermanagement.security.config;

import com.im.usermanagement.model.Role;               // CORRECTED: Removed redundant Usermanagement
import com.im.usermanagement.model.RoleName;           // CORRECTED
import com.im.usermanagement.repository.RoleRepository; // CORRECTED
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

/**
 * Component to ensure default application roles are present in the database on startup.
 * Implements CommandLineRunner to execute logic immediately after the application context is loaded.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

    // Inject the RoleRepository to interact with the database
    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("Checking for default roles in database...");

        // Iterate through all defined role names (ROLE_USER, ROLE_ADMIN)
        Arrays.stream(RoleName.values()).forEach(roleName -> {
            // Check if the role already exists in the database
            if (roleRepository.findByName(roleName).isEmpty()) {
                // If not found, create and save the new Role entity
                Role role = new Role();
                role.setName(roleName);
                roleRepository.save(role);
                log.info("Created new role: {}", roleName);
            } else {
                log.info("Role already exists: {}", roleName);
            }
        });

        log.info("Default roles check complete. User registration should now proceed without error.");
    }
}