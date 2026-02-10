package com.omatheusmesmo.shoppmate.user.service;

import com.omatheusmesmo.shoppmate.user.entity.User;
import com.omatheusmesmo.shoppmate.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;

    public User addUser(User user) {
        isUserValid(user);
        encryptPassword(user);
        userRepository.save(user);
        return user;
    }

    private void encryptPassword(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
    }

    public User editUser(User user) {
        findUserById(user.getId());
        validateIfDataIsNullOrEmpty(user);
        encryptPassword(user);
        userRepository.save(user);
        return user;
    }

    public void removeUser(Long id) {
        findUserById(id);
        userRepository.deleteById(id);
    }

    public User findUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new NoSuchElementException("User not found!"));
    }

    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new NoSuchElementException("User not found!"));
    }

    // TODO: create a method to validate new and old users to avoid duplicating
    public void isUserValid(User user) {
        validateIfDataIsNullOrEmpty(user);
        // validateIfUserExists(user.getEmail());
    }

    public void validateIfUserExists(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("E-mail is already being used!");
        }
    }

    public void validateIfDataIsNullOrEmpty(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalArgumentException("E-mail is required!");
        }

        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required!");
        }
    }

    public List<User> findUsers() {
        return userRepository.findAll();
    }

    public User findUser(Long id) {
        return userRepository.findById(id).orElseThrow();
    }

    public List<User> returnAllUsers() {
        return userRepository.findAll();
    }
}
