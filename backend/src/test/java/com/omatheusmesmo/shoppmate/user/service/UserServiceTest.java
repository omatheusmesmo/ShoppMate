package com.omatheusmesmo.shoppmate.user.service;

import com.omatheusmesmo.shoppmate.shared.testutils.UserTestFactory;
import com.omatheusmesmo.shoppmate.user.dtos.RegisterUserDTO;
import com.omatheusmesmo.shoppmate.user.entity.User;
import com.omatheusmesmo.shoppmate.user.repository.UserRepository;
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
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User userMock;

    @BeforeEach
    void setUp() {
        userMock = UserTestFactory.createValidUser();
    }

    @AfterEach
    void tearDown() {
        userMock = null;
        reset(userRepository);
    }

    @Test
    void addUser_ValidDTO_ReturnsSavedUser() {
        // Arrange
        String encodedPassword = "encoded-" + userMock.getPassword();
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(passwordEncoder.encode(anyString())).thenReturn(encodedPassword);

        RegisterUserDTO registerDTO = new RegisterUserDTO(userMock.getEmail(), userMock.getFullName(),
                userMock.getPassword());

        // Act
        User result = userService.addUser(registerDTO);

        // Assert
        assertNotNull(result);
        assertEquals(userMock.getEmail(), result.getEmail());
        assertEquals(userMock.getFullName(), result.getFullName());
        assertEquals(encodedPassword, result.getPassword());
        assertEquals("USER", result.getRole());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void addUser_EmailAlreadyUsed_ThrowsIllegalArgumentException() {
        // Arrange
        when(userRepository.findByEmail(userMock.getEmail())).thenReturn(Optional.of(userMock));
        RegisterUserDTO registerDTO = new RegisterUserDTO(userMock.getEmail(), userMock.getFullName(),
                userMock.getPassword());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.addUser(registerDTO));
        assertEquals("E-mail is already being used!", exception.getMessage());
        verify(userRepository, times(1)).findByEmail(userMock.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void validateIfUserExists_UserDoesNotExist_NoExceptionThrown() {
        // Arrange
        when(userRepository.findByEmail(userMock.getEmail())).thenReturn(Optional.empty());

        // Act & Assert
        assertDoesNotThrow(() -> userService.validateIfUserExists(userMock.getEmail()));
        verify(userRepository, times(1)).findByEmail(userMock.getEmail());
    }

    @Test
    void validateIfUserExists_UserExists_ThrowsIllegalArgumentException() {
        // Arrange
        when(userRepository.findByEmail(userMock.getEmail())).thenReturn(Optional.of(userMock));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.validateIfUserExists(userMock.getEmail()));
        assertEquals("E-mail is already being used!", exception.getMessage());
    }

    @Test
    void validateIfDataIsNullOrEmpty_ValidData_NoExceptionThrown() {
        // Act & Assert
        assertDoesNotThrow(() -> userService.validateIfDataIsNullOrEmpty(userMock));
    }

    private void assertValidationException(User user, String expectedMessage) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.validateIfDataIsNullOrEmpty(user));
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void validateIfDataIsNullOrEmpty_EmailIsEmpty_ThrowsIllegalArgumentException() {
        userMock.setEmail(" ");
        assertValidationException(userMock, "E-mail is required!");
    }

    @Test
    void validateIfDataIsNullOrEmpty_EmailIsNull_ThrowsIllegalArgumentException() {
        userMock.setEmail(null);
        assertValidationException(userMock, "E-mail is required!");
    }

    @Test
    void validateIfDataIsNullOrEmpty_PasswordIsEmpty_ThrowsIllegalArgumentException() {
        userMock.setPassword(" ");
        assertValidationException(userMock, "Password is required!");
    }

    @Test
    void validateIfDataIsNullOrEmpty_PasswordIsNull_ThrowsIllegalArgumentException() {
        userMock.setPassword(null);
        assertValidationException(userMock, "Password is required!");
    }

    @Test
    void editUser_ExistingId_ReturnsUpdatedUser() {
        // Arrange
        when(userRepository.findById(userMock.getId())).thenReturn(Optional.of(userMock));
        when(userRepository.save(any(User.class))).thenReturn(userMock);

        // Act
        User result = userService.editUser(userMock);

        // Assert
        assertNotNull(result);
        assertEquals(userMock, result);
        verify(userRepository, times(1)).findById(userMock.getId());
        verify(userRepository, times(1)).save(userMock);
    }

    @Test
    void editUser_UserNotFound_ThrowsNoSuchElementException() {
        // Arrange
        when(userRepository.findById(userMock.getId())).thenReturn(Optional.empty());

        // Act & Assert
        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> userService.editUser(userMock));
        assertEquals("User not found!", exception.getMessage());
    }

    @Test
    void editUser_EmailNull_ThrowsIllegalArgumentException() {
        when(userRepository.findById(userMock.getId())).thenReturn(Optional.of(userMock));
        userMock.setEmail(null);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.editUser(userMock));
        assertEquals("E-mail is required!", exception.getMessage());
    }

    @Test
    void editUser_PasswordNull_ThrowsIllegalArgumentException() {
        when(userRepository.findById(userMock.getId())).thenReturn(Optional.of(userMock));
        userMock.setPassword(null);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.editUser(userMock));
        assertEquals("Password is required!", exception.getMessage());
    }

    @Test
    void editUser_EmailBlank_ThrowsIllegalArgumentException() {
        when(userRepository.findById(userMock.getId())).thenReturn(Optional.of(userMock));
        userMock.setEmail(" ");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.editUser(userMock));
        assertEquals("E-mail is required!", exception.getMessage());
    }

    @Test
    void editUser_PasswordBlank_ThrowsIllegalArgumentException() {
        when(userRepository.findById(userMock.getId())).thenReturn(Optional.of(userMock));
        userMock.setPassword(" ");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.editUser(userMock));
        assertEquals("Password is required!", exception.getMessage());
    }

    @Test
    void findUserById_ExistingId_ReturnsUser() {
        // Arrange
        when(userRepository.findById(userMock.getId())).thenReturn(Optional.of(userMock));

        // Act
        User result = userService.findUserById(userMock.getId());

        // Assert
        assertEquals(userMock, result);
        verify(userRepository, times(1)).findById(userMock.getId());
    }

    @Test
    void findUserById_NonExistingId_ThrowsNoSuchElementException() {
        // Arrange
        when(userRepository.findById(userMock.getId())).thenReturn(Optional.empty());

        // Act & Assert
        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> userService.findUserById(userMock.getId()));
        assertEquals("User not found!", exception.getMessage());
    }

    @Test
    void removeUser_ExistingId_DeletesUser() {
        // Arrange
        when(userRepository.findById(userMock.getId())).thenReturn(Optional.of(userMock));

        // Act
        assertDoesNotThrow(() -> userService.removeUser(userMock.getId()));

        // Assert
        verify(userRepository, times(1)).findById(userMock.getId());
        verify(userRepository, times(1)).deleteById(userMock.getId());
    }

    @Test
    void returnAllUsers_ExistingUsers_ReturnsList() {
        // Arrange
        when(userRepository.findAll()).thenReturn(List.of(userMock));

        // Act
        List<User> result = userService.returnAllUsers();

        // Assert
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(userRepository, times(1)).findAll();
    }
}
