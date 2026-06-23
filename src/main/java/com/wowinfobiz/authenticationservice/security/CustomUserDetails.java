package com.wowinfobiz.authenticationservice.security;

import com.wowinfobiz.authenticationservice.enums.AccessPrincipalType;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class CustomUserDetails implements UserDetails {

    private final UUID principalId;
    private final String email;
    private final String passwordHash;
    private final AccessPrincipalType principalType;
    private final boolean active;

    public CustomUserDetails(
            UUID principalId,
            String email,
            String passwordHash,
            AccessPrincipalType principalType,
            boolean active
    ) {
        this.principalId = principalId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.principalType = principalType;
        this.active = active;
    }

    public UUID getPrincipalId() {
        return principalId;
    }

    public AccessPrincipalType getPrincipalType() {
        return principalType;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + principalType.name()));
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
        return true;
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
