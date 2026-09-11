package com.azneotech.productcatalogservice.exceptions;

import lombok.Getter;

@Getter
public class FakeStoreApiProductException extends RuntimeException {

    private final FakeStoreApiExceptionType exceptionType;

    public FakeStoreApiProductException(String message, FakeStoreApiExceptionType exceptionType) {
        super(message);
        this.exceptionType = exceptionType;
    }

}
