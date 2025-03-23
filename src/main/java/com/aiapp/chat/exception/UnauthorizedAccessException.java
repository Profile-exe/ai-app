package com.aiapp.chat.exception;

import com.aiapp.common.exception.BusinessException;

public class UnauthorizedAccessException extends BusinessException {

    public static final BusinessException EXCEPTION = new UnauthorizedAccessException();

    private UnauthorizedAccessException() {
        super(ThreadErrorCode.UNAUTHORIZED_ACCESS);
    }
}
