package com.omatheusmesmo.shoppmate.user.mapper;

import com.omatheusmesmo.shoppmate.user.dtos.UserResponseDTO;
import com.omatheusmesmo.shoppmate.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponseDTO toResponseDTO(User user) {
        if (user == null) {
            return null;
        }

        return new UserResponseDTO(
                user.getId(),
                user.getFullName(),
                user.getEmail()
        );
    }

}