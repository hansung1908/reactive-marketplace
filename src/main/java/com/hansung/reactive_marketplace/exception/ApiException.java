package com.hansung.reactive_marketplace.exception;

public class ApiException extends RuntimeException {
    private final ExceptionMessage exception;

    public ApiException(ExceptionMessage exception) {
        this.exception = exception;
    }

    public ExceptionMessage getException() {
        return exception;
    }
}
