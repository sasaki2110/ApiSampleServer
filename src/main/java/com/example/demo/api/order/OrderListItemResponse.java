package com.example.demo.api.order;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 受注一覧の1行。ヘッダに複数明細がある場合は明細行数ぶん同じヘッダ値が繰り返される。
 */
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
    LocalDateTime createdAt,
    Long lineId,
    int lineNo,
    String productCode,
    String productName,
    int quantity,
    int unitPrice,
    int amount
) {}
