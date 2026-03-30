package com.example.demo.api.order;

public record OrderCreateResponse(
    Long orderId,
    String orderNumber,
    String message
) {}
