package com.aiapp.auth.jwt.exception;

import com.aiapp.common.exception.BusinessException;

public class MissingTokenException extends BusinessException {

    public static final BusinessException EXCEPTION = new MissingTokenException();

    private MissingTokenException() {
        super(JwtErrorCode.MISSING_TOKEN);
    }
}
