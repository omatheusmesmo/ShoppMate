package com.omatheusmesmo.shoppmate.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omatheusmesmo.shoppmate.auth.service.JwtService;
import com.omatheusmesmo.shoppmate.user.entity.User;
import com.omatheusmesmo.shoppmate.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setFullName("John Doe");
        user.setEmail("john@example.com");
        user.setPassword("pass");
    }

    @Test
    @WithMockUser
    void getUsers() throws Exception {
        when(userService.findUsers()).thenReturn(List.of(user));

        mockMvc.perform(get("/users/users")).andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(user))));
    }

    @Test
    @WithMockUser
    void getUserById() throws Exception {
        when(userService.findUser(1L)).thenReturn(user);

        mockMvc.perform(get("/users/userDetailsService/1")).andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(user)));
    }
}
