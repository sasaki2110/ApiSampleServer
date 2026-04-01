package com.example.demo.api.order;

import java.time.LocalDate;
import java.util.List;

public record OrderDetailResponse(
    Long id,
    String orderNumber,
    String contractPartyCode,
    String deliveryPartyCode,
    String deliveryLocation,
    LocalDate dueDate,
    String forecastNumber,
    List<OrderDetailLineResponse> lines
) {
    public record OrderDetailLineResponse(
        Long lineId,
        int lineNo,
        String productCode,
        String productName,
        int quantity,
        int unitPrice,
        int amount
    ) {}
}
