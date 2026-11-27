package com.farmwise.backend.config;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    private static final Logger log = LoggerFactory.getLogger(JwtProperties.class);

    private String secret = "";
    private long accessTokenMinutes = 15;
    private long refreshTokenDays = 7;
    private String issuer = "farmwise-api";
    private boolean allowEphemeralDevSecret = false;

    private volatile byte[] resolvedSigningKey;

    @PostConstruct
    void validateAndInitializeKey() {
        byte[] keyBytes = secret == null ? new byte[0] : secret.trim().getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length >= 32) {
            this.resolvedSigningKey = keyBytes;
            return;
        }
        if (!allowEphemeralDevSecret) {
            throw new IllegalStateException(
                    "JWT_SECRET must be set to at least 32 characters in non-development profiles.");
        }
        byte[] generated = new byte[48];
        new SecureRandom().nextBytes(generated);
        this.resolvedSigningKey = Base64.getEncoder().encode(generated);
        log.warn("JWT_SECRET not provided; generated ephemeral 384-bit signing key for dev/test runtime.");
    }

    public byte[] signingKeyBytes() {
        if (resolvedSigningKey == null) {
            validateAndInitializeKey();
        }
        return resolvedSigningKey;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getAccessTokenMinutes() {
        return accessTokenMinutes;
    }

    public void setAccessTokenMinutes(long accessTokenMinutes) {
        this.accessTokenMinutes = accessTokenMinutes;
    }

    public long getRefreshTokenDays() {
        return refreshTokenDays;
    }

    public void setRefreshTokenDays(long refreshTokenDays) {
        this.refreshTokenDays = refreshTokenDays;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public boolean isAllowEphemeralDevSecret() {
        return allowEphemeralDevSecret;
    }

    public void setAllowEphemeralDevSecret(boolean allowEphemeralDevSecret) {
        this.allowEphemeralDevSecret = allowEphemeralDevSecret;
    }
}
