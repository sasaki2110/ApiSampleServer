package com.example.demo.api.error;

import java.util.List;

public record ApiErrorResponse(
    String code,
    String message,
    List<FieldErrorDetail> details
) {
    public record FieldErrorDetail(String field, String reason) {}
}
