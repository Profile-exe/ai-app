package com.aiapp.auth.exception;

import com.aiapp.common.exception.BusinessException;
import com.aiapp.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {
    ACCESS_DENIED("A001", HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    ;

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
