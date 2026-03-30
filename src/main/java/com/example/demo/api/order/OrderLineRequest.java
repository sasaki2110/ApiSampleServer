package com.example.demo.api.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderLineRequest(
    @NotBlank String productCode,
    String productName,
    @NotNull @Positive Integer quantity,
    @NotNull @Positive Integer unitPrice,
    @NotNull Integer amount
) {}
