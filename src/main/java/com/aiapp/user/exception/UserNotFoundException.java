package com.aiapp.user.exception;

import com.aiapp.common.exception.BusinessException;

public class UserNotFoundException extends BusinessException {

    public static final BusinessException EXCEPTION = new UserNotFoundException();

    private UserNotFoundException() {
        super(UserErrorCode.USER_NOT_FOUND);
    }
}
