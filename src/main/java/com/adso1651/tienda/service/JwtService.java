package com.adso1651.tienda.service;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Value("${security.jwt.token-expiration}")
    private Long tokenExpiration;

    private SecretKey getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Genera un JWT firmado: username como subject y userId / rolId como claims del payload.
     */
    public String generateToken(Long userId, String username, Long rolId) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("rolId", rolId)
                .issuedAt(new Date(now))
                .expiration(new Date(now + tokenExpiration))
                .signWith(getSignKey())
                .compact();
    }

    /**
     * Valida firma y expiración y devuelve los claims.
     * Lanza ExpiredJwtException si expiró y JwtException / IllegalArgumentException si es inválido.
     */
    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsername(Claims claims) {
        return claims.getSubject();
    }

    public Long extractUserId(Claims claims) {
        return toLong(claims.get("userId"));
    }

    public Long extractRolId(Claims claims) {
        return toLong(claims.get("rolId"));
    }

    // Jackson deserializa los números pequeños como Integer, por eso se convierte vía Number
    private Long toLong(Object value) {
        return value instanceof Number number ? number.longValue() : null;
    }
}
