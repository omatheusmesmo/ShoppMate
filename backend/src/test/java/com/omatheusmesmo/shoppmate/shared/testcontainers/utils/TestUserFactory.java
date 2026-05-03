package com.omatheusmesmo.shoppmate.shared.testcontainers.utils;

import com.omatheusmesmo.shoppmate.auth.service.JwtService;
import com.omatheusmesmo.shoppmate.user.entity.User;
import com.omatheusmesmo.shoppmate.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class TestUserFactory {

    public static final String TEST_USER_EMAIL = "user@gmail.com";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public TestUserFactory(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public String createTokenForTestUser() {
        String email = TEST_USER_EMAIL;

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setFullName("Test User");
            newUser.setPassword(passwordEncoder.encode("123456"));
            newUser.setRole("USER");
            return userRepository.save(newUser);
        });

        var userDetails = org.springframework.security.core.userdetails.User.builder().username(user.getEmail())
                .password(user.getPassword()).roles(user.getRole()).build();

        return jwtService.generateToken(userDetails);
    }
}
