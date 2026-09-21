package com.azneotech.productcatalogservice.exceptions;

import lombok.Getter;

@Getter
public class CategoryException extends RuntimeException {

    private final CategoryExceptionType exceptionType;

    public CategoryException(String message, CategoryExceptionType exceptionType) {
        super(message);
        this.exceptionType = exceptionType;
    }

}
