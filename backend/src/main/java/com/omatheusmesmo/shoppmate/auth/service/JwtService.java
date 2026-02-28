package com.omatheusmesmo.shoppmate.auth.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    private MACSigner signer;
    private MACVerifier verifier;

    @Value("${jwt.token.expiration}")
    private long tokenExpiration;

    @Value("${jwt.secret.key}")
    private String secretKey;

    public JwtService() {
        // Signer and verifier will be initialized after secretKey is injected
        this.signer = null;
        this.verifier = null;
    }

    private void initSignerAndVerifier() throws JOSEException {
        if (this.signer == null && this.verifier == null && this.secretKey != null) {
            this.signer = new MACSigner(this.secretKey);
            this.verifier = new MACVerifier(this.secretKey);
        }
    }

    public String generateToken(UserDetails userDetails) {
        try {
            initSignerAndVerifier();
            SignedJWT signedJWT = new SignedJWT(buildHeader(), buildToken(userDetails));
            signedJWT.sign(signer);
            return signedJWT.serialize();
        } catch (JOSEException e) {
            logger.error("Failed to sign token", e);
            throw new JwtServiceException("Failed to sign token", e);
        }
    }

    public boolean validateToken(String token) {
        try {
            initSignerAndVerifier();
            SignedJWT signedJWT = SignedJWT.parse(token);

            // Verify signature
            if (!signedJWT.verify(verifier)) {
                logger.warn("Token signature verification failed");
                return false;
            }

            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

            Date expirationTime = claims.getExpirationTime();
            if (expirationTime == null || expirationTime.before(new Date())) {
                logger.warn("Token has expired or expiration time is missing. Expiration: {}", expirationTime);
                return false;
            }

            Date notBeforeTime = claims.getNotBeforeTime();
            if (notBeforeTime != null && notBeforeTime.after(new Date())) {
                logger.warn("Token not yet valid (not before time). Not Before: {}", notBeforeTime);
                return false;
            }

            logger.debug("Token validation successful for subject: {}", claims.getSubject());
            return true;

        } catch (ParseException e) {

            logger.error("Failed to parse JWT token string during validation: {}", e.getMessage());
            return false;
        } catch (JOSEException e) {
            logger.error("Failed to verify JWT token signature during validation: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("Unexpected error during JWT validation: {}", e.getMessage(), e);
            return false;
        }
    }

    public JWTClaimsSet decryptToken(String token) {
        try {
            initSignerAndVerifier();
            SignedJWT signedJWT = SignedJWT.parse(token);

            // Verify signature
            if (!signedJWT.verify(verifier)) {
                throw new JwtServiceException("Token signature verification failed", null);
            }

            return signedJWT.getJWTClaimsSet();
        } catch (Exception e) {
            logger.error("Failed to verify and decrypt token", e);
            throw new JwtServiceException("Failed to verify and decrypt token", e);
        }
    }

    private JWSHeader buildHeader() {
        return new JWSHeader.Builder(JWSAlgorithm.HS256).build();
    }

    private JWTClaimsSet buildToken(UserDetails userDetails) {
        return new JWTClaimsSet.Builder().subject(userDetails.getUsername())
                .expirationTime(new Date(new Date().getTime() + tokenExpiration)).notBeforeTime(new Date())
                .jwtID(UUID.randomUUID().toString()).build();
    }

}
