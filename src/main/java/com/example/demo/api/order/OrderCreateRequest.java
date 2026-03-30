package com.example.demo.api.order;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record OrderCreateRequest(
    @NotBlank String contractPartyCode,
    @NotBlank String deliveryPartyCode,
    String deliveryLocation,
    LocalDate dueDate,
    String forecastNumber,
    @NotEmpty @Valid List<OrderLineRequest> lines
) {}
