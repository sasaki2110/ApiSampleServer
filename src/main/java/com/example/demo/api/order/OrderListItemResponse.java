package com.example.demo.api.order;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record OrderListItemResponse(
    Long id,
    String orderNumber,
    String contractPartyCode,
    String contractPartyName,
    String deliveryPartyCode,
    String deliveryPartyName,
    String deliveryLocation,
    LocalDate dueDate,
    String forecastNumber,
    int totalAmount,
    int lineCount,
    LocalDateTime createdAt
) {}
