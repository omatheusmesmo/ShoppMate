package com.omatheusmesmo.shoppmate.IntegrationTests.utils;

import com.omatheusmesmo.shoppmate.user.entity.User;
import com.omatheusmesmo.shoppmate.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class TestUserFactory {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void createUserIfNotExists() {
        if (userRepository.findByEmail("user@gmail.com").isEmpty()) {

            User user = new User();
            user.setEmail("user@gmail.com");
            user.setFullName("New User For Test");
            user.setPassword(passwordEncoder.encode("123456"));
            user.setRole("USER");

            userRepository.save(user);
        }
    }
}