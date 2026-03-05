package com.omatheusmesmo.shoppmate.user.controller;

import com.omatheusmesmo.shoppmate.user.entity.User;
import com.omatheusmesmo.shoppmate.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// TODO: use DTOs instead of entity
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    private final UserDetailsService userDetailsService;

    public UserController(UserService userService, UserDetailsService userDetailsService) {
        this.userService = userService;
        this.userDetailsService = userDetailsService;
    }

    // TODO: Corrigir erro: org.springframework.web.servlet.resource.NoResourceFoundException: No static resource users.
    // O mapeamento atual está gerando /users/users em vez de /users.
    @Operation(summary = "Return all users")
    @GetMapping("/users")
    public List<User> getUsers() {
        return userService.findUsers();
    }

    // TODO: maybe remove the "/userDetailsService/" to avoid redundancy
    @Operation(summary = "Return a user by id")
    @GetMapping("/userDetailsService/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.findUser(id);
    }
}
