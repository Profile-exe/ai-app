package com.aiapp.user.exception;

import com.aiapp.common.exception.BusinessException;

public class AlreadyExistEmailException extends BusinessException {

    public static final BusinessException EXCEPTION = new AlreadyExistEmailException();

    private AlreadyExistEmailException() {
        super(UserErrorCode.ALREADY_EXIST_EMAIL);
    }
}
