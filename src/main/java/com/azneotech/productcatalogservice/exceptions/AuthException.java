package com.azneotech.productcatalogservice.exceptions;

import lombok.Getter;

@Getter
public class AuthException extends RuntimeException {

    private final AuthExceptionType exceptionType;

    public AuthException(String message, AuthExceptionType exceptionType) {
        super(message);
        this.exceptionType = exceptionType;
    }

    public AuthException(String message, AuthExceptionType exceptionType, Throwable cause) {
        super(message, cause);
        this.exceptionType = exceptionType;
    }

}
