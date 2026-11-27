package com.farmwise.backend.security;

import com.farmwise.backend.config.JwtProperties;
import com.farmwise.backend.user.UserEntity;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.text.ParseException;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final JwtProperties jwtProperties;
    private final Clock utcClock;

    public JwtService(JwtProperties jwtProperties, Clock utcClock) {
        this.jwtProperties = jwtProperties;
        this.utcClock = utcClock;
    }

    public record ParsedToken(UUID userId, String email, String primaryRole, List<String> roles, Instant expiresAt) {
    }

    public String generateAccessToken(UserEntity user, Set<String> roles) {
        Instant now = Instant.now(utcClock);
        Instant expiresAt = now.plusSeconds(jwtProperties.getAccessTokenMinutes() * 60L);
        List<String> roleList = roles.stream().sorted().toList();

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(user.getId().toString())
                .issuer(jwtProperties.getIssuer())
                .issueTime(Date.from(now))
                .expirationTime(Date.from(expiresAt))
                .jwtID(UUID.randomUUID().toString())
                .claim("email", user.getEmail())
                .claim("role", user.getPrimaryRole())
                .claim("roles", roleList)
                .claim("type", "access")
                .build();

        try {
            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
            signedJWT.sign(new MACSigner(jwtProperties.signingKeyBytes()));
            return signedJWT.serialize();
        } catch (JOSEException ex) {
            throw new IllegalStateException("Failed to sign JWT access token", ex);
        }
    }

    public Optional<ParsedToken> parseAndVerifyAccessToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            MACVerifier verifier = new MACVerifier(jwtProperties.signingKeyBytes());
            if (!signedJWT.verify(verifier)) {
                return Optional.empty();
            }
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            if (!jwtProperties.getIssuer().equals(claims.getIssuer())) {
                return Optional.empty();
            }
            if (!"access".equals(claims.getStringClaim("type"))) {
                return Optional.empty();
            }
            Date exp = claims.getExpirationTime();
            Instant now = Instant.now(utcClock);
            if (exp == null || !exp.toInstant().isAfter(now)) {
                return Optional.empty();
            }
            UUID userId = UUID.fromString(claims.getSubject());
            String email = claims.getStringClaim("email");
            String role = claims.getStringClaim("role");
            List<String> roles = claims.getStringListClaim("roles");
            return Optional.of(new ParsedToken(userId, email, role, roles == null ? List.of() : roles, exp.toInstant()));
        } catch (ParseException | JOSEException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    public long getAccessTokenExpiresInSeconds() {
        return jwtProperties.getAccessTokenMinutes() * 60L;
    }
}
