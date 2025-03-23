package com.aiapp.auth.jwt.service;


import com.aiapp.auth.jwt.AuthToken;
import com.aiapp.auth.jwt.exception.InvalidRefreshTokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class AuthTokenService {

    private static final String GRANT_TYPE = "Bearer";

    @Value("${custom.jwt.access-token-expire-time}")
    private Long accessTokenExpireTime;

    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public AuthToken generateAuthToken(Long userId) {
        String accessToken = jwtTokenProvider.generateAccessToken(userId);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userId);

        return AuthToken.of(accessToken, refreshToken, GRANT_TYPE, accessTokenExpireTime);
    }

    @Transactional
    public AuthToken reissue(String refreshToken) {
        Long userId = jwtTokenProvider.getUserIdFromRefreshToken(refreshToken);

        if (!jwtTokenProvider.existsByUserIdAndRefreshToken(refreshToken)) {
            throw InvalidRefreshTokenException.EXCEPTION;
        }

        return generateAuthToken(userId);
    }

    @Transactional
    public void logout(Long userId) {
        jwtTokenProvider.deleteByUserId(userId);
    }
}
