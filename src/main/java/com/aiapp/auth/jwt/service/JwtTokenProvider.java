package com.aiapp.auth.jwt.service;

import com.aiapp.auth.jwt.RefreshToken;
import com.aiapp.auth.jwt.TokenType;
import com.aiapp.auth.jwt.exception.ExpiredTokenException;
import com.aiapp.auth.jwt.exception.InvalidAccessTokenException;
import com.aiapp.auth.jwt.exception.InvalidRefreshTokenException;
import com.aiapp.auth.jwt.exception.MissingTokenException;
import com.aiapp.auth.jwt.repository.TokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.time.Duration;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private static final String BEARER_PREFIX = "Bearer ";

    private final SecretKey accessSecretKey;
    private final SecretKey refreshSecretKey;
    private final Long accessTokenExpireTime;
    private final Long refreshTokenExpireTime;
    private final TokenRepository tokenRepository;

    public JwtTokenProvider(
            @Value("${custom.jwt.access-secret-key}") String accessSecretKey,
            @Value("${custom.jwt.refresh-secret-key}") String refreshSecretKey,
            @Value("${custom.jwt.access-token-expire-time}") Long accessTokenExpireTime,
            @Value("${custom.jwt.refresh-token-expire-time}") Long refreshTokenExpireTime,
            TokenRepository tokenRepository
    ) {
        this.accessSecretKey = Keys.hmacShaKeyFor(accessSecretKey.getBytes());
        this.refreshSecretKey = Keys.hmacShaKeyFor(refreshSecretKey.getBytes());
        this.accessTokenExpireTime = accessTokenExpireTime;
        this.refreshTokenExpireTime = refreshTokenExpireTime;
        this.tokenRepository = tokenRepository;
    }

    public String generateAccessToken(Long userId) {
        Date now = new Date();
        return Jwts.builder()
                .claim("id", userId)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + Duration.ofSeconds(accessTokenExpireTime).toMillis()))
                .signWith(accessSecretKey)  // JWS(JSON Web Signature)를 생성하기 위한 key 설정
                .compact();
    }

    public String generateRefreshToken(Long userId) {
        Date now = new Date();
        String refreshToken = Jwts.builder()
                .claim("id", userId)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + Duration.ofSeconds(refreshTokenExpireTime).toMillis()))
                .signWith(refreshSecretKey)  // JWS(JSON Web Signature)를 생성하기 위한 key 설정
                .compact();

        RefreshToken token = new RefreshToken(userId, refreshToken, refreshTokenExpireTime);
        tokenRepository.save(token);    // 이미 저장되어있는 토큰이 있다면 덮어씌움

        return refreshToken;
    }

    public Long getUserIdFromAccessToken(String accessToken) {
        Claims claims = parseClaims(accessToken, TokenType.ACCESS);
        return claims.get("id", Long.class);
    }

    public Long getUserIdFromRefreshToken(String refreshToken) {
        Claims claims = parseClaims(refreshToken, TokenType.REFRESH);
        return claims.get("id", Long.class);
    }

    public String extractToken(String header) {
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            throw MissingTokenException.EXCEPTION;
        }
        return header.substring(BEARER_PREFIX.length());
    }

    public boolean validateRefreshToken(String token) {
        parseClaims(token, TokenType.REFRESH);
        return true;
    }

    public boolean existsByUserIdAndRefreshToken(String refreshToken) {
        return tokenRepository.existsByToken(refreshToken);
    }

    private Claims parseClaims(String token, TokenType tokenType) {
        SecretKey secretKey = tokenType.equals(TokenType.ACCESS) ? accessSecretKey : refreshSecretKey;
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw ExpiredTokenException.EXCEPTION;
        } catch (Exception e) {
            if (tokenType.equals(TokenType.ACCESS)) {
                throw InvalidAccessTokenException.EXCEPTION;
            }
            throw InvalidRefreshTokenException.EXCEPTION;
        }
    }

    public void deleteByUserId(Long userId) {
        tokenRepository.deleteById(userId);
    }
}
