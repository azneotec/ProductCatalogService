package com.azneotech.productcatalogservice.controllers;

import com.azneotech.productcatalogservice.exceptions.FakeStoreApiProductException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ControllerAdvisor {

    @ExceptionHandler({IllegalArgumentException.class, NullPointerException.class})
    private ResponseEntity<String> handleIllegalArgumentException(Exception exception) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(FakeStoreApiProductException.class)
    private ResponseEntity<String> handleFakeStoreApiProductException(FakeStoreApiProductException exception) {
        return switch (exception.getExceptionType()) {
            case PRODUCT_NOT_FOUND -> new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
            case PRODUCT_ALREADY_EXISTS -> new ResponseEntity<>(exception.getMessage(), HttpStatus.CONFLICT);
        };
    }

    @ExceptionHandler(RuntimeException.class)
    private ResponseEntity<String> handleRuntimeException(Exception exception) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
    }

}
