package com.aiapp.auth.exception;

import com.aiapp.common.exception.BusinessException;

public class AccessDeniedException extends BusinessException {

    public static final BusinessException EXCEPTION = new AccessDeniedException();

    private AccessDeniedException() {
        super(AuthErrorCode.ACCESS_DENIED);
    }
}
