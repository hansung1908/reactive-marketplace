package com.hansung.reactive_marketplace.exception;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {
    private final ExceptionMessage exception;

    public ApiException(ExceptionMessage exception) {
        this.exception = exception;
    }
}
