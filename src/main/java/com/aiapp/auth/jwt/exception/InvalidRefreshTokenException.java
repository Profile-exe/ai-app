package com.aiapp.auth.jwt.exception;

import com.aiapp.common.exception.BusinessException;

public class InvalidRefreshTokenException extends BusinessException {

    public static final BusinessException EXCEPTION = new InvalidRefreshTokenException();

    private InvalidRefreshTokenException() {
        super(JwtErrorCode.INVALID_REFRESH_TOKEN);
    }
}
