package com.aiapp.user.exception;

import com.aiapp.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {
    INVALID_PASSWORD("USER_001", HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
    ALREADY_EXIST_EMAIL("USER_002", HttpStatus.BAD_REQUEST, "이미 존재하는 이메일입니다."),
    USER_NOT_FOUND("USER_003", HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    ;

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
