package com.omatheusmesmo.shoppmate.auth.service;

import com.nimbusds.jwt.JWTClaimsSet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class JwtServiceTest {

    @Mock
    private UserDetails userDetails;

    private JwtService jwtService;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);

        KeyPair keyPair = generateRSAKeys();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();

        long tokenExpiration = 3600000L;
        jwtService = new JwtService(publicKey, privateKey, tokenExpiration);

        when(userDetails.getUsername()).thenReturn("testuser");
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    void shouldGenerateAndValidateValidToken() {
        String token = jwtService.generateToken(userDetails);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(jwtService.validateToken(token), "Generated token should be valid");
    }

    @Test
    void shouldDecryptTokenAndExtractUsername() {
        String token = jwtService.generateToken(userDetails);
        JWTClaimsSet claimsSet = jwtService.decryptToken(token);

        assertEquals("testuser", claimsSet.getSubject());
        assertNotNull(claimsSet.getExpirationTime());
        assertNotNull(claimsSet.getJWTID());
    }

    @Test
    void shouldGenerateUniqueJwtIdsForDifferentTokens() {
        String token1 = jwtService.generateToken(userDetails);
        String token2 = jwtService.generateToken(userDetails);

        JWTClaimsSet claims1 = jwtService.decryptToken(token1);
        JWTClaimsSet claims2 = jwtService.decryptToken(token2);

        assertNotEquals(claims1.getJWTID(), claims2.getJWTID());
    }

    @Test
    void shouldNotValidateExpiredToken() throws InterruptedException {
        JwtService shortLivedService = createServiceWithExpiration(1L);
        String token = shortLivedService.generateToken(userDetails);

        Thread.sleep(10);

        assertFalse(shortLivedService.validateToken(token));
    }

    @Test
    void shouldNotValidateInvalidTokens() {
        assertFalse(jwtService.validateToken("invalid_token"));
        assertFalse(jwtService.validateToken(""));
        assertFalse(jwtService.validateToken(null));
    }

    @Test
    void shouldNotValidateMalformedJweToken() {
        String malformedJwe = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

        assertFalse(jwtService.validateToken(malformedJwe));
    }

    @Test
    void shouldNotValidateTokenEncryptedWithDifferentKeys() {
        JwtService otherService = createServiceWithExpiration(3600000L);
        String token = otherService.generateToken(userDetails);

        assertFalse(jwtService.validateToken(token));
    }

    @Test
    void shouldThrowExceptionWhenDecryptingInvalidToken() {
        assertThrows(JwtServiceException.class, () -> jwtService.decryptToken("invalid"));
    }

    @Test
    void shouldUseDifferentUsernameForToken() {
        when(userDetails.getUsername()).thenReturn("anotheruser");

        String token = jwtService.generateToken(userDetails);
        JWTClaimsSet claims = jwtService.decryptToken(token);

        assertEquals("anotheruser", claims.getSubject());
    }

    private JwtService createServiceWithExpiration(long expiration) {
        KeyPair keyPair = generateRSAKeys();
        return new JwtService(
                (RSAPublicKey) keyPair.getPublic(),
                (RSAPrivateKey) keyPair.getPrivate(),
                expiration);
    }

    private KeyPair generateRSAKeys() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.genKeyPair();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate RSA keys", e);
        }
    }
}
