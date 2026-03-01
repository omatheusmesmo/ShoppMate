package com.omatheusmesmo.shoppmate.user.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Pattern;

public record RegisterUserDTO(String email,

        @JsonProperty("full_name") String fullName,

        @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$", message = "Password must contain at least one uppercase letter, one special character and a number!") String password) {
}
