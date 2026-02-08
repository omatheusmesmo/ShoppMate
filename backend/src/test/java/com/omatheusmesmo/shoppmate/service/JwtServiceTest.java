package com.omatheusmesmo.shoppmate.service;

import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.omatheusmesmo.shoppmate.auth.service.JwtService;
import com.omatheusmesmo.shoppmate.auth.service.JwtServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.KeyPair;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtServiceTest {

    @Mock
    private UserDetails userDetails;

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        jwtService = Mockito.spy(new JwtService());  // instanciando a classe que será testada
        when(userDetails.getUsername()).thenReturn("testuser");
        when(userDetails.isCredentialsNonExpired()).thenReturn(true);
    }

    @Test
    void shouldGenerateToken() {
        String token = jwtService.generateToken(userDetails);

        assertNotNull(token, "Token should not be null");
        assertFalse(token.isEmpty(), "Token should not be empty");
    }

   //TODO FIX THIS
    void shouldFailToValidateInvalidToken() {
        String invalidToken = "invalid_token";

        assertThrows(JwtServiceException.class, () -> {
            jwtService.validateToken(invalidToken);
        }, "Invalid token should throw JwtServiceException");
    }

    @Test
    void shouldDecryptTokenCorrectly() {
        String token = jwtService.generateToken(userDetails);

        JWTClaimsSet claimsSet = jwtService.decryptToken(token);

        assertNotNull(claimsSet, "JWTClaimsSet should not be null");
        assertEquals("testuser", claimsSet.getSubject(), "Token subject should be 'testuser'");
    }

    @Test
    void shouldFailToDecryptInvalidToken() {
        String invalidToken = "invalid_token";

        assertThrows(JwtServiceException.class, () -> {
            jwtService.decryptToken(invalidToken);
        }, "Invalid token should throw JwtServiceException during decryption");
    }

    @Test
    void shouldGenerateRSAKeyPair() {
        // Teste para verificar se a geração da chave RSA ocorre corretamente
        KeyPair keyPair = jwtService.generateRSAKeys();

        assertNotNull(keyPair, "RSA KeyPair should not be null");
        assertNotNull(keyPair.getPrivate(), "Private key should not be null");
        assertNotNull(keyPair.getPublic(), "Public key should not be null");
    }

    void shouldThrowExceptionWhenKeyGenerationFails() {
        // Simulando falha na geração de chave para testar o manejo de exceção
        JwtService faultyJwtService = new JwtService() {
            @Override
            public KeyPair generateRSAKeys() {
                throw new RuntimeException("Key generation failed");
            }
        };

        assertThrows(JwtServiceException.class, faultyJwtService::generateRSAKeys, "Key generation failure should throw exception");
    }

    void shouldBuildTokenWithCorrectExpirationTime() {
        String token = jwtService.generateToken(userDetails);

        EncryptedJWT encryptedJWT;
        try {
            encryptedJWT = EncryptedJWT.parse(token);
            JWTClaimsSet claimsSet = encryptedJWT.getJWTClaimsSet();

            assertNotNull(claimsSet.getExpirationTime(), "Token should have an expiration time");
            assertTrue(claimsSet.getExpirationTime().after(new Date()), "Token expiration time should be in the future");
        } catch (Exception e) {
            fail("Parsing the generated token failed", e);
        }
    }

    void shouldValidateValidToken() {
        String token = jwtService.generateToken(userDetails);

        boolean isValid = jwtService.validateToken(token);

        assertTrue(isValid, "The token should be valid");
    }

}
