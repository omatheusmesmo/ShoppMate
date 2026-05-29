package com.omatheusmesmo.shoppmate.shared.test.annotation;

import com.omatheusmesmo.shoppmate.shared.test.security.WithMockCustomUserSecurityContextFactory;
import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory.class)
public @interface WithMockCustomUser {
    long id() default 1L;

    String email() default "test@example.com";

    String fullName() default "Test User";

    String role() default "USER";
}
