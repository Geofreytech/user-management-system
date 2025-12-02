package com.im.usermanagement.service;

import com.im.usermanagement.model.User;
import com.im.usermanagement.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    // Inject the UserRepository (assuming you already have this repository interface)
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Locates the user based on the username (email).
     * This method is executed by Spring Security's AuthenticationProvider during login.
     *
     * @param username The email provided by the user during login.
     * @return UserDetails object (which is your User entity, now implementing UserDetails)
     * @throws UsernameNotFoundException if the user is not found in the database.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Find the user by their email
        User user = userRepository.findByEmail(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with email: " + username)
                );

        // Return the User object which implements UserDetails
        return user;
    }
}