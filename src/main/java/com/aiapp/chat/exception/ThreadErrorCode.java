package com.aiapp.chat.exception;

import com.aiapp.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ThreadErrorCode implements ErrorCode {
    THREAD_NOT_FOUND("THREAD_001", HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다."),
    UNAUTHORIZED_ACCESS("THREAD_002", HttpStatus.FORBIDDEN, "채팅방에 접근할 권한이 없습니다."),
    ;

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
