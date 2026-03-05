package com.omatheusmesmo.shoppmate.auth.controller;

import com.omatheusmesmo.shoppmate.user.dtos.RegisterUserDTO;
import com.omatheusmesmo.shoppmate.user.dtos.UserResponseDTO;
import com.omatheusmesmo.shoppmate.auth.dtos.LoginRequest;
import com.omatheusmesmo.shoppmate.user.entity.User;
import com.omatheusmesmo.shoppmate.auth.service.JwtService;
import com.omatheusmesmo.shoppmate.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    private final JwtService jwtService;

    private final AuthenticationManager authenticationManager;

    private final UserDetailsService userDetailsService;

    public AuthController(UserService userService, JwtService jwtService, AuthenticationManager authenticationManager,
            UserDetailsService userDetailsService) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
    }

    @Operation(summary = "Register a User")
    @PostMapping("/sign")
    public ResponseEntity<User> registerUser(@Valid @RequestBody RegisterUserDTO dto) {
        var registeredUser = userService.addUser(dto);
        return ResponseEntity.ok(registeredUser);
    }

    @Operation(summary = "User's login")
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {

        UserDetails user = userDetailsService.loadUserByUsername(loginRequest.getEmail());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        String token = jwtService.generateToken(user);

        User userSingned = userService.findUserByEmail(loginRequest.getEmail());

        UserResponseDTO reponse = new UserResponseDTO(userSingned.getId(), userSingned.getFullName(),
                userSingned.getEmail());

        return ResponseEntity.ok(token);
    }
}
