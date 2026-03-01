package com.omatheusmesmo.shoppmate.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omatheusmesmo.shoppmate.auth.service.JwtService;
import com.omatheusmesmo.shoppmate.user.dtos.RegisterUserDTO;
import com.omatheusmesmo.shoppmate.user.entity.User;
import com.omatheusmesmo.shoppmate.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void ShouldReturnOk_WhenPasswordFollowsRequirements() throws Exception {
        var dto = new RegisterUserDTO("anakin@skywalker.com", "Anakin Skywalker", "CorrectPass@123");

        mockMvc.perform(post("/auth/sign").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(dto))).andExpect(status().isOk());
    }

    @Test
    void ShouldReturnBadRequest_WhenPasswordDoesNotContainUppercaseLetters() throws Exception {
        var dto = new RegisterUserDTO("anakin@skywalker.com", "Anakin Skywalker", "password-123");

        mockMvc.perform(post("/auth/sign").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(dto))).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation Failed")).andExpect(jsonPath("$.details").value(
                        "password: Password must contain at least one uppercase letter, one special character and a number!"));
    }

    @Test
    void ShouldReturnBadRequest_WhenPasswordDoesNotContainSpecialCharacters() throws Exception {
        var dto = new RegisterUserDTO("anakin@skywalker.com", "Anakin Skywalker", "Password123");

        mockMvc.perform(post("/auth/sign").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(dto))).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation Failed")).andExpect(jsonPath("$.details").value(
                        "password: Password must contain at least one uppercase letter, one special character and a number!"));
    }

    @Test
    void ShouldReturnBadRequest_WhenPasswordDoesNotContainNumbers() throws Exception {
        var dto = new RegisterUserDTO("anakin@skywalker.com", "Anakin Skywalker", "Password@");

        mockMvc.perform(post("/auth/sign").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(dto))).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation Failed")).andExpect(jsonPath("$.details").value(
                        "password: Password must contain at least one uppercase letter, one special character and a number!"));
    }
}
