package com.aiapp.auth.jwt.exception;

import com.aiapp.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum JwtErrorCode implements ErrorCode {
    INVALID_ACCESS_TOKEN("JW001", HttpStatus.UNAUTHORIZED, "올바른 ACCESS 토큰이 아닙니다."),
    INVALID_REFRESH_TOKEN("JW002", HttpStatus.UNAUTHORIZED, "올바른 REFRESH 토큰이 아닙니다."),
    EXPIRED_TOKEN("JW003", HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),
    MISSING_TOKEN("JW004", HttpStatus.UNAUTHORIZED, "요청에 토큰이 포함되어있지 않습니다."),
    ;

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
