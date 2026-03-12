package com.omatheusmesmo.shoppmate.auth.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
public class RsaKeyConfig {

    @Value("${jwt.private-key}")
    private Resource privateKeyResource;

    @Value("${jwt.public-key}")
    private Resource publicKeyResource;

    @Bean
    public RSAPrivateKey privateKey() throws Exception {
        validateResource(privateKeyResource, "private-key");
        String key = new String(Files.readAllBytes(privateKeyResource.getFile().toPath()));
        key = key.replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", "").replaceAll("\\s",
                "");

        byte[] decoded = Base64.getDecoder().decode(key);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return (RSAPrivateKey) factory.generatePrivate(keySpec);
    }

    @Bean
    public RSAPublicKey publicKey() throws Exception {
        validateResource(publicKeyResource, "public-key");
        String key = new String(Files.readAllBytes(publicKeyResource.getFile().toPath()));
        key = key.replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "").replaceAll("\\s",
                "");

        byte[] decoded = Base64.getDecoder().decode(key);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return (RSAPublicKey) factory.generatePublic(keySpec);
    }

    private void validateResource(Resource resource, String keyType) throws Exception {
        if (resource == null || !resource.exists()) {
            throw new IllegalStateException("JWT " + keyType + " file not found: " + resource);
        }
        String protocol = resource.getURL().getProtocol();
        if ("classpath".equals(protocol) || "jar".equals(protocol)) {
            throw new IllegalStateException(
                    "JWT " + keyType + " MUST be provided as an external file, not from classpath.");
        }
    }
}
