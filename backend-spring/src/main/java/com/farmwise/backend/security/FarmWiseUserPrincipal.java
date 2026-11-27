package com.farmwise.backend.security;

import com.farmwise.backend.user.UserEntity;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class FarmWiseUserPrincipal implements UserDetails {

    private final UUID userId;
    private final String email;
    private final String passwordHash;
    private final String fullName;
    private final String primaryRole;
    private final boolean active;
    private final boolean emailVerified;
    private final Set<String> roleNames;
    private final Collection<GrantedAuthority> authorities;

    public FarmWiseUserPrincipal(UserEntity user) {
        this.userId = user.getId();
        this.email = user.getEmail();
        this.passwordHash = user.getPasswordHash();
        this.fullName = user.getFullName();
        this.primaryRole = user.getPrimaryRole();
        this.active = user.isActive();
        this.emailVerified = user.isEmailVerified();
        this.roleNames = user.getRoles().stream()
                .map(r -> r.getName().toUpperCase())
                .collect(Collectors.toSet());
        if (this.roleNames.isEmpty() && primaryRole != null) {
            this.roleNames.add(primaryRole.toUpperCase());
        }
        this.authorities = this.roleNames.stream()
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role))
                .toList();
    }

    public UUID getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPrimaryRole() {
        return primaryRole;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public Set<String> getRoleNames() {
        return roleNames;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return active;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
