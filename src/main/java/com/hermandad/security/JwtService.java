package com.hermandad.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String secret;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(AuthenticatedUser user) {
        return Jwts.builder().subject(user.getId().toString())
                .claim("tokenVersion", user.getTokenVersion())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 86400000L))
                .signWith(getSigningKey()).compact();
    }

    public TokenIdentity validateToken(String token) {
        Claims claims = Jwts.parser().verifyWith(getSigningKey()).build()
                .parseSignedClaims(token).getPayload();
        Long version = claims.get("tokenVersion", Long.class);
        // Reject legacy tokens and tokens without an expiration date.
        if (version == null || version < 0 || claims.getExpiration() == null
                || claims.getSubject() == null) {
            throw new IllegalArgumentException("Token incompleto");
        }
        long id = Long.parseLong(claims.getSubject());
        if (id <= 0) throw new IllegalArgumentException("Identidad inválida");
        return new TokenIdentity(id, version);
    }

    public record TokenIdentity(long userId, long tokenVersion) {}
}