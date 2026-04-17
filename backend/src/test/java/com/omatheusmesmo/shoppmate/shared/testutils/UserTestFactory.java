package com.omatheusmesmo.shoppmate.shared.testutils;

import com.omatheusmesmo.shoppmate.user.dtos.RegisterUserDTO;
import com.omatheusmesmo.shoppmate.user.entity.User;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

public class UserTestFactory {

    private static final AtomicLong ID_GENERATOR = new AtomicLong(1);

    public static User createValidUser() {
        User user = new User();
        user.setId(ID_GENERATOR.getAndIncrement());
        user.setFullName(FakerUtil.getFaker().name().fullName());
        user.setEmail(FakerUtil.getFaker().internet().emailAddress());
        user.setPassword("password");
        user.setRole("USER");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setDeleted(false);
        return user;
    }

    public static RegisterUserDTO createValidRegisterUserDTO() {
        return new RegisterUserDTO(FakerUtil.getFaker().internet().emailAddress(),
                FakerUtil.getFaker().name().fullName(), generateValidPassword());
    }

    private static String generateValidPassword() {
        // Generates a password with at least one uppercase, one digit, and one special character
        // to satisfy the @Pattern regex in RegisterUserDTO.
        return FakerUtil.getFaker().internet().password(10, 20, true, true, true) + "A1!";
    }
}
