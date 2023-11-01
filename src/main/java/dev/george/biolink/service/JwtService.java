package dev.george.biolink.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtService {

    @Value("${biolink.jwt.secret}")
    private String jwtSecret;

    public Integer getIdFromToken(String token) {
        try {
            return Integer.parseInt(getClaimFromToken(token, Claims::getSubject));
        } catch (NumberFormatException exc) {
            return null;
        }
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(getAllClaimsFromToken(token));
    }

    public Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(jwtSecret)
                .build()
                .parseSignedClaims(token)
                .getBody();
    }

    public String generateToken(Map<String, Object> claims, String subject) {
        return generateToken(claims, subject, 86400L * 1000L * 30L);
    }

    public String generateToken(Map<String, Object> claims, String subject, long tokenDuration) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(Date.from(Instant.now()))
                .expiration(new Date(System.currentTimeMillis() + tokenDuration))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }
}
