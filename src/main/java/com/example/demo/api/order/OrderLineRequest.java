package com.example.demo.api.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderLineRequest(
    @NotBlank(message = "製品コードは必須です") String productCode,
    String productName,
    @NotNull(message = "数量は必須です") @Positive(message = "数量は1以上の整数を入力してください") Integer quantity,
    @NotNull(message = "単価は必須です") @Positive(message = "単価は1以上の整数を入力してください") Integer unitPrice,
    @NotNull(message = "金額は必須です") Integer amount
) {}
