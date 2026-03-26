package com.example.demo.api.error;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<ApiErrorResponse.FieldErrorDetail> details = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(err -> new ApiErrorResponse.FieldErrorDetail(err.getField(), err.getDefaultMessage()))
            .toList();

        var body = new ApiErrorResponse(
            "VALIDATION_ERROR",
            "入力値に誤りがあります",
            details
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}
