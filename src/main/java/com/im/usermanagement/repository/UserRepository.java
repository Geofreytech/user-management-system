package com.im.usermanagement.repository;

import com.im.usermanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional; // Crucial import for this method

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Existing method
    boolean existsByEmail(String email);

    // ⭐ FIX: UNCOMMENT THIS LINE AND ENSURE IT'S ACTIVE CODE!
    Optional<User> findByEmail(String email);
}