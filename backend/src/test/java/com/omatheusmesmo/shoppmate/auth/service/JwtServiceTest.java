package com.omatheusmesmo.shoppmate.auth.service;

import com.nimbusds.jwt.JWTClaimsSet;
import com.omatheusmesmo.shoppmate.shared.testutils.FakerUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class JwtServiceTest {

    @Mock
    private UserDetails userDetails;

    private JwtService jwtService;
    private AutoCloseable mocks;
    private String agnosticUsername;
    private static final String SECRET_KEY = "0123456789012345678901234567890123456789012345678901234567890123"; // 64
                                                                                                                 // chars
                                                                                                                 // for
                                                                                                                 // HS256

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        agnosticUsername = FakerUtil.getFaker().internet().username();

        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "tokenExpiration", 3600000L);
        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET_KEY);

        when(userDetails.getUsername()).thenReturn(agnosticUsername);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    void generateToken_ValidUserDetails_ReturnsValidToken() {
        // Act
        String token = jwtService.generateToken(userDetails);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(jwtService.validateToken(token), "Generated token should be valid");
    }

    @Test
    void decryptToken_ValidToken_ExtractsUsername() {
        // Arrange
        String token = jwtService.generateToken(userDetails);

        // Act
        JWTClaimsSet claimsSet = jwtService.decryptToken(token);

        // Assert
        assertEquals(agnosticUsername, claimsSet.getSubject());
        assertNotNull(claimsSet.getExpirationTime());
        assertNotNull(claimsSet.getJWTID());
    }

    @Test
    void generateToken_MultipleCalls_ReturnsUniqueJwtIds() {
        // Act
        String token1 = jwtService.generateToken(userDetails);
        String token2 = jwtService.generateToken(userDetails);

        JWTClaimsSet claims1 = jwtService.decryptToken(token1);
        JWTClaimsSet claims2 = jwtService.decryptToken(token2);

        // Assert
        assertNotEquals(claims1.getJWTID(), claims2.getJWTID());
    }

    @Test
    void validateToken_ExpiredToken_ReturnsFalse() {
        // Arrange
        JwtService shortLivedService = new JwtService();
        ReflectionTestUtils.setField(shortLivedService, "tokenExpiration", -1000L);
        ReflectionTestUtils.setField(shortLivedService, "secretKey", SECRET_KEY);

        String token = shortLivedService.generateToken(userDetails);

        // Act & Assert
        assertFalse(shortLivedService.validateToken(token), "Expired token should not be valid");
    }

    @Test
    void validateToken_InvalidInputs_ReturnsFalse() {
        // Act & Assert
        assertFalse(jwtService.validateToken("invalid_token"));
        assertFalse(jwtService.validateToken(""));
        assertFalse(jwtService.validateToken(null));
    }

    @Test
    void validateToken_MalformedJwe_ReturnsFalse() {
        // Arrange
        String malformedJwe = String.join(".", "not", "a", "valid", "jwe", "token");

        // Act & Assert
        assertFalse(jwtService.validateToken(malformedJwe));
    }

    @Test
    void validateToken_DifferentKeys_ReturnsFalse() {
        // Arrange
        JwtService otherService = new JwtService();
        ReflectionTestUtils.setField(otherService, "tokenExpiration", 3600000L);
        ReflectionTestUtils.setField(otherService, "secretKey",
                "different_secret_key_different_secret_key_different_secret_key_64_chars");
        String token = otherService.generateToken(userDetails);

        // Act & Assert
        assertFalse(jwtService.validateToken(token));
    }

    @Test
    void decryptToken_InvalidToken_ThrowsException() {
        // Act & Assert
        assertThrows(JwtServiceException.class, () -> jwtService.decryptToken("invalid"));
    }

    @Test
    void generateToken_DifferentUser_ReturnsTokenWithCorrectSubject() {
        // Arrange
        String anotherUser = agnosticUsername + "-different";
        when(userDetails.getUsername()).thenReturn(anotherUser);

        // Act
        String token = jwtService.generateToken(userDetails);
        JWTClaimsSet claims = jwtService.decryptToken(token);

        // Assert
        assertEquals(anotherUser, claims.getSubject());
    }
}
