package com.azneotech.productcatalogservice.controllers;

import com.azneotech.productcatalogservice.exceptions.CategoryException;
import com.azneotech.productcatalogservice.exceptions.ProductException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ControllerAdvisor {

    // MethodArgumentTypeMismatchException covers e.g. ?categoryId=abc; without it that
    // RuntimeException subclass would fall through to the 404 handler below.
    @ExceptionHandler({IllegalArgumentException.class, NullPointerException.class,
            MethodArgumentTypeMismatchException.class})
    private ResponseEntity<String> handleIllegalArgumentException(Exception exception) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ProductException.class)
    private ResponseEntity<String> handleProductException(ProductException exception) {
        return switch (exception.getExceptionType()) {
            case PRODUCT_NOT_FOUND -> new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
            case PRODUCT_ALREADY_EXISTS -> new ResponseEntity<>(exception.getMessage(), HttpStatus.CONFLICT);
        };
    }

    @ExceptionHandler(CategoryException.class)
    private ResponseEntity<String> handleCategoryException(CategoryException exception) {
        return switch (exception.getExceptionType()) {
            case CATEGORY_NOT_FOUND -> new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
        };
    }

    @ExceptionHandler(RuntimeException.class)
    private ResponseEntity<String> handleRuntimeException(Exception exception) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
    }

}
