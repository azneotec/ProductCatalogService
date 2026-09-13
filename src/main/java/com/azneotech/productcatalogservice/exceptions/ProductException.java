package com.azneotech.productcatalogservice.exceptions;

import lombok.Getter;

@Getter
public class ProductException extends RuntimeException {

    private final ProductExceptionType exceptionType;

    public ProductException(String message, ProductExceptionType exceptionType) {
        super(message);
        this.exceptionType = exceptionType;
    }

}
