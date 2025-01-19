package com.backend.homework.infrastructure.security;

import com.backend.homework.application.dto.TokenPair;
import com.backend.homework.common.exception.ApplicationException;
import com.backend.homework.common.exception.ExceptionCase;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtManager {

    private final Key jwtSecretKey;
    private final Long accessTokenExpiration;
    private final Long refreshTokenExpiration;

    public JwtManager(@Value("${jwt.secret}") String secretKey,
                        @Value("${jwt.access.expiration}") String accessTokenExpiration,
                        @Value("${jwt.refresh.expiration}") String refreshTokenExpiration) {
        this.jwtSecretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = Long.valueOf(accessTokenExpiration);
        this.refreshTokenExpiration = Long.valueOf(refreshTokenExpiration);
    }

    public TokenPair generateTokenPair(UserDetailsImpl userDetails) {
        long currentTime = (new Date()).getTime();
        String accessToken = generateAccessToken(userDetails, currentTime);
        String refreshToken = generateRefreshToken(userDetails, currentTime);
        return new TokenPair(accessToken, refreshToken);
    }

    private String generateAccessToken(UserDetailsImpl userDetails, long currentTime) {
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setExpiration(new Date(currentTime + accessTokenExpiration))
                .signWith(jwtSecretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    private String generateRefreshToken(UserDetailsImpl userDetails, long currentTime) {
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setExpiration(new Date(currentTime + refreshTokenExpiration))
                .signWith(jwtSecretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .setSigningKey(jwtSecretKey)
                .parseClaimsJws(token)
                .getBody();
    }

    public void validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(jwtSecretKey).build().parseClaimsJws(token);
        } catch (ExpiredJwtException e) {
            throw new ApplicationException(ExceptionCase.EXPIRED_TOKEN);
        } catch (MalformedJwtException | UnsupportedJwtException | SignatureException e) {
            throw new ApplicationException(ExceptionCase.INVALID_TOKEN);
        }
    }

}
