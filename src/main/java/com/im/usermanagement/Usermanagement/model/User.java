package com.im.usermanagement.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "app_user")
@Data
@NoArgsConstructor
@AllArgsConstructor
// ⭐ NEW: Implement the UserDetails interface
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    // ⭐ NEW: Field to store the HASHED password
    @Column(nullable = false)
    private String password;

    // ⭐ NEW: Relationship to Roles (Many-to-Many is standard)
    @ManyToMany(fetch = FetchType.EAGER) // Fetch roles immediately on user load
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private List<Role> roles;

    // Existing field (rename to match UserDetails isEnabled for consistency)
    private boolean isActive = true;

    // ----------------------------------------------------------------------
    // Implementation of UserDetails methods
    // ----------------------------------------------------------------------

    // ⭐ 1. Returns the collection of authorities (roles) granted to the user.
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles;
    }

    // ⭐ 2. Returns the username used to authenticate the user. We use the email.
    @Override
    public String getUsername() {
        return email;
    }

    // NOTE: getPassword() is handled by Lombok's @Data annotation.

    // ⭐ 3. Indicates whether the user's account has expired.
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // ⭐ 4. Indicates whether the user is locked or unlocked.
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // ⭐ 5. Indicates whether the user's credentials (password) has expired.
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // ⭐ 6. Indicates whether the user is enabled or disabled (we use the existing isActive).
    @Override
    public boolean isEnabled() {
        return isActive;
    }
}