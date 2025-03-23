package com.aiapp.common.config;

import com.aiapp.auth.annotation.AllowAnonymous;
import com.aiapp.auth.jwt.service.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private static final String REISSUE_URI = "/api/auth/reissue";

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler
    ) {
        // CORS preflight 요청은 토큰 검증을 하지 않음
        if (CorsUtils.isPreFlightRequest(request)) {
            return true;
        }

        // AllowAnonymous 어노테이션이 붙어있는 경우 토큰 검증을 하지 않음
        if (handler instanceof HandlerMethod handlerMethod && handlerMethod.getMethodAnnotation(AllowAnonymous.class) != null) {
            return true;
        }

        String token = jwtTokenProvider.extractToken(request.getHeader(HttpHeaders.AUTHORIZATION));

        // reissue 엔드포인트로 요청이 들어오면 refresh token 검증
        if (request.getRequestURI().equals(REISSUE_URI)) {
            return jwtTokenProvider.validateRefreshToken(token);
        }

        // Access Token에서 userId 추출 후 Refresh Token이 tokenRepository에 존재하는지 확인
        Long userId = jwtTokenProvider.getUserIdFromAccessToken(token);

        return jwtTokenProvider.existsByMemberId(userId);
    }
}

