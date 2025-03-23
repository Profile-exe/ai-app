package com.aiapp.chat.exception;

import com.aiapp.common.exception.BusinessException;

public class ThreadNotFoundException extends BusinessException {

    public static final BusinessException EXCEPTION = new ThreadNotFoundException();

    private ThreadNotFoundException() {
        super(ThreadErrorCode.THREAD_NOT_FOUND);
    }
}
