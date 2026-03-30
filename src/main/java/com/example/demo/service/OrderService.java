package com.example.demo.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.api.order.OrderCreateRequest;
import com.example.demo.api.order.OrderCreateResponse;
import com.example.demo.api.order.OrderLineRequest;
import com.example.demo.model.OrderHeader;
import com.example.demo.model.OrderLine;
import com.example.demo.repository.OrderHeaderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final DateTimeFormatter ORDER_DATE = DateTimeFormatter.BASIC_ISO_DATE;

    private final OrderHeaderRepository orderHeaderRepository;

    @Transactional
    public OrderCreateResponse createOrder(OrderCreateRequest request) {
        if (request.lines() == null || request.lines().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "明細が1行以上必要です");
        }

        var header = new OrderHeader();
        header.setOrderNumber(nextOrderNumber());
        header.setContractPartyCode(request.contractPartyCode().trim());
        header.setDeliveryPartyCode(request.deliveryPartyCode().trim());
        header.setDeliveryLocation(blankToNull(request.deliveryLocation()));
        header.setDueDate(request.dueDate());
        header.setForecastNumber(blankToNull(request.forecastNumber()));

        int lineNo = 1;
        for (OrderLineRequest lineReq : request.lines()) {
            if (lineReq.productCode() == null || lineReq.productCode().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "製品コードが空の明細は登録できません");
            }
            int expectedAmount = lineReq.quantity() * lineReq.unitPrice();
            if (!Objects.equals(lineReq.amount(), expectedAmount)) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "明細の金額が数量×単価と一致しません: line " + lineNo
                );
            }

            var line = new OrderLine();
            line.setLineNo(lineNo++);
            line.setProductCode(lineReq.productCode().trim());
            line.setProductName(blankToNull(lineReq.productName()));
            line.setQuantity(lineReq.quantity());
            line.setUnitPrice(lineReq.unitPrice());
            line.setAmount(lineReq.amount());
            header.addLine(line);
        }

        var saved = orderHeaderRepository.save(header);
        return new OrderCreateResponse(
            saved.getId(),
            saved.getOrderNumber(),
            "受注を登録しました"
        );
    }

    private static String blankToNull(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        return s;
    }

    private String nextOrderNumber() {
        LocalDate today = LocalDate.now();
        String prefix = "ORD-" + today.format(ORDER_DATE) + "-";
        long seq = orderHeaderRepository.countByOrderNumberStartingWith(prefix) + 1;
        if (seq > 999) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "本日の受注採番上限に達しました");
        }
        return prefix + String.format("%03d", seq);
    }
}
