package com.example.demo.api.order;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record OrderCreateRequest(
    @NotBlank(message = "契約先コードは必須です") String contractPartyCode,
    @NotBlank(message = "納入先コードは必須です") String deliveryPartyCode,
    String deliveryLocation,
    LocalDate dueDate,
    String forecastNumber,
    @NotEmpty(message = "明細は1行以上必要です") @Valid List<OrderLineRequest> lines
) {}
