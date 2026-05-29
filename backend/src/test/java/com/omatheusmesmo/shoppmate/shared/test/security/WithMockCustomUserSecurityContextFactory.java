package com.omatheusmesmo.shoppmate.shared.test.security;

import com.omatheusmesmo.shoppmate.shared.test.annotation.WithMockCustomUser;
import com.omatheusmesmo.shoppmate.user.entity.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.List;

public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser annotation) {
        User user = new User();
        user.setId(annotation.id());
        user.setEmail(annotation.email());
        user.setFullName(annotation.fullName());
        user.setRole(annotation.role());

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null,
                List.of(new SimpleGrantedAuthority("ROLE_" + annotation.role())));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        return context;
    }
}
