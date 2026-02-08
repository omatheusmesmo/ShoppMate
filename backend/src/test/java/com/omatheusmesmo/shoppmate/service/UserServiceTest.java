package com.omatheusmesmo.shoppmate.service;

import com.omatheusmesmo.shoppmate.user.entity.User;
import com.omatheusmesmo.shoppmate.user.repository.UserRepository;
import com.omatheusmesmo.shoppmate.user.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    User userMock;

    @BeforeEach
    void setUp() {
        userMock = new User("John Doe","John@Doe.com", "1234","USER");
        userMock.setId(1L);
    }

    @AfterEach
    void tearDown() {
        userMock = null;
        reset(userRepository);
    }

    @Test
    void addUser() {
        when(userRepository.findByEmail(userMock.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(userMock);

        User user = userService.addUser(userMock);

        assertEquals(userMock, user);
        verify(userRepository, times(1)).findByEmail(userMock.getEmail());
        verify(userRepository, times(1)).save(userMock);
    }

    @Test
    void addUserWithEmailUsed() {
        when(userRepository.findByEmail(userMock.getEmail())).thenReturn(Optional.of(userMock));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.addUser(userMock));
        assertEquals("E-mail is already being used!", exception.getMessage());

        verify(userRepository, times(1)).findByEmail(userMock.getEmail());
    }

    @Test
    void validateIfUserExists() {
        assertDoesNotThrow(() -> userService.validateIfUserExists(userMock.getEmail()));
        verify(userRepository, times(1)).findByEmail(userMock.getEmail());
    }

    @Test
    void validateIfUserExistsUserUsed() {
        when(userRepository.findByEmail(userMock.getEmail())).thenReturn(Optional.of(userMock));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.validateIfUserExists(userMock.getEmail()));
        assertEquals("E-mail is already being used!", exception.getMessage());

        verify(userRepository).findByEmail(userMock.getEmail());
    }

    @Test
    void validateIfDataIsNullOrEmpty() {
        assertDoesNotThrow(() -> userService.validateIfDataIsNullOrEmpty(userMock));
    }

    private void assertValidationException(User user, String expectedMessage) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.validateIfDataIsNullOrEmpty(user));
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void validateIfDataIsNullOrEmptyEmailIsEmpty() {
        userMock.setEmail(" ");
        assertValidationException(userMock, "E-mail is required!");
    }

    @Test
    void validateIfDataIsNullOrEmptyEmailIsNull() {
        userMock.setEmail(null);
        assertValidationException(userMock, "E-mail is required!");
    }

    @Test
    void validateIfDataIsNullOrEmptyPasswordIsEmpty() {
        userMock.setPassword(" ");
        assertValidationException(userMock, "Password is required!");
    }

    @Test
    void validateIfDataIsNullOrEmptyPasswordIsNull() {
        userMock.setPassword(null);
        assertValidationException(userMock, "Password is required!");
    }

    @Test
    void editUser() {
        when(userRepository.findById(userMock.getId())).thenReturn(Optional.of(userMock));
        when(userRepository.save(any(User.class))).thenReturn(userMock);

        User user = userService.editUser(userMock);

        assertNotNull(user);
        assertEquals(userMock, user);

        verify(userRepository, times(1)).findById(userMock.getId());
        verify(userRepository, times(1)).save(userMock);
    }

    @Test
    void editUserWithoutExistingId() {
        when(userRepository.findById(userMock.getId())).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> userService.editUser(userMock));
        assertEquals("User not found!", exception.getMessage());

        verify(userRepository, times(1)).findById(userMock.getId());
    }

    @Test
    void editUserWithEmailNull() {
        when(userRepository.findById(userMock.getId())).thenReturn(Optional.of(userMock));
        userMock.setEmail(null);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.editUser(userMock));
        assertEquals("E-mail is required!", exception.getMessage());
    }

    @Test
    void editUserWithPasswordNull() {
        when(userRepository.findById(userMock.getId())).thenReturn(Optional.of(userMock));
        userMock.setPassword(null);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.editUser(userMock));
        assertEquals("Password is required!", exception.getMessage());
    }

    @Test
    void editUserWithEmailBlank() {
        when(userRepository.findById(userMock.getId())).thenReturn(Optional.of(userMock));
        userMock.setEmail(" ");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.editUser(userMock));
        assertEquals("E-mail is required!", exception.getMessage());
    }

    @Test
    void editUserWithPasswordBlank() {
        when(userRepository.findById(userMock.getId())).thenReturn(Optional.of(userMock));
        userMock.setPassword(" ");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.editUser(userMock));
        assertEquals("Password is required!", exception.getMessage());
    }

    @Test
    void testFindUserById() {
        when(userRepository.findById(userMock.getId())).thenReturn(Optional.of(userMock));

        assertDoesNotThrow(() -> userService.findUserById(userMock.getId()));

        verify(userRepository, times(1)).findById(userMock.getId());
    }

    @Test
    void testFindUserByIdThrowsExceptionWhenUserNotFound() {
        when(userRepository.findById(userMock.getId())).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> userService.findUserById(userMock.getId()));
        assertEquals("User not found!", exception.getMessage());

        verify(userRepository, times(1)).findById(userMock.getId());
    }

    @Test
    void removeUser() {
        when(userRepository.findById(userMock.getId())).thenReturn(Optional.of(userMock));
        assertDoesNotThrow(() -> userService.removeUser(userMock.getId()));

        verify(userRepository, times(1)).findById(userMock.getId());
        verify(userRepository, times(1)).deleteById(userMock.getId());
    }

    @Test
    void returnAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(userMock));

        List<User> list = userService.returnAllUsers();

        verify(userRepository, times(1)).findAll();
    }
}
