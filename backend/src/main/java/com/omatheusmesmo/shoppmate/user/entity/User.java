package com.omatheusmesmo.shoppmate.user.entity;

import java.util.Collection;
import java.util.Collections;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.omatheusmesmo.shoppmate.shared.domain.BaseAuditableEntity;
import com.omatheusmesmo.shoppmate.user.dtos.RegisterUserDTO;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "users")
public class User extends BaseAuditableEntity implements UserDetails {

    private String email;

    @Column(name = "full_name")
    private String fullName;

    private String password;
    private String role = "USER";

    public User(RegisterUserDTO dto) {
        this.email = dto.email();
        this.fullName = dto.fullName();
        this.password = dto.password();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(role));
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
        return true;
    }
}
