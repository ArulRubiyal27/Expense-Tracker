package com.expensetracker.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component

public class JwtTokenProvider {
    private final SecretKey secretKey;
    @Getter
    private final long jwtExpirationInMs;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long jwtExpirationInMs) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.jwtExpirationInMs = jwtExpirationInMs;
    }

    public String generateToken(String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder().subject(email)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    public Claims getClaimsFromToken(String token) {
        return Jwts.parser ()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
