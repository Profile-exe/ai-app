package com.aiapp.user.exception;

import com.aiapp.common.exception.BusinessException;

public class InvalidPasswordException extends BusinessException {

    public static final BusinessException EXCEPTION = new InvalidPasswordException();

    private InvalidPasswordException() {
        super(UserErrorCode.INVALID_PASSWORD);
    }
}
