package com.example.demo.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.api.order.OrderCreateRequest;
import com.example.demo.api.order.OrderCreateResponse;
import com.example.demo.api.order.OrderDetailResponse;
import com.example.demo.api.order.OrderDetailResponse.OrderDetailLineResponse;
import com.example.demo.api.order.OrderLineRequest;
import com.example.demo.api.order.OrderListItemResponse;
import com.example.demo.model.OrderHeader;
import com.example.demo.model.OrderLine;
import com.example.demo.repository.OrderHeaderRepository;
import com.example.demo.repository.OrderHeaderSpecifications;
import com.example.demo.repository.PartyRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final DateTimeFormatter ORDER_DATE = DateTimeFormatter.BASIC_ISO_DATE;

    private final OrderHeaderRepository orderHeaderRepository;
    private final PartyRepository partyRepository;

    @Transactional(readOnly = true)
    public List<OrderListItemResponse> searchOrders(
        String orderNumber,
        String contractPartyCode,
        LocalDate dueDateFrom,
        LocalDate dueDateTo
    ) {
        Specification<OrderHeader> spec = Specification.allOf(
            OrderHeaderSpecifications.fetchLines(),
            OrderHeaderSpecifications.orderNumberContains(orderNumber),
            OrderHeaderSpecifications.contractPartyCodeEquals(contractPartyCode),
            OrderHeaderSpecifications.dueDateFrom(dueDateFrom),
            OrderHeaderSpecifications.dueDateTo(dueDateTo)
        );
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        List<OrderHeader> headers = orderHeaderRepository.findAll(spec, sort);

        var codes = new HashSet<String>();
        for (OrderHeader h : headers) {
            codes.add(h.getContractPartyCode());
            codes.add(h.getDeliveryPartyCode());
        }
        Map<String, String> nameByCode = partyRepository.findByCodeIn(codes).stream()
            .collect(Collectors.toMap(p -> p.getCode(), p -> p.getName()));

        return headers.stream().flatMap(h -> expandHeaderToLines(h, nameByCode)).toList();
    }

    @Transactional(readOnly = true)
    public OrderDetailResponse getOrder(Long id) {
        OrderHeader header = orderHeaderRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "受注が見つかりません"));
        return toDetailResponse(header);
    }

    @Transactional
    public void deleteOrder(Long id) {
        if (!orderHeaderRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "受注が見つかりません");
        }
        orderHeaderRepository.deleteById(id);
    }

    private static Stream<OrderListItemResponse> expandHeaderToLines(
        OrderHeader h,
        Map<String, String> nameByCode
    ) {
        List<OrderLine> lines = h.getLines().stream()
            .sorted(Comparator.comparing(OrderLine::getLineNo))
            .toList();
        int totalAmount = lines.stream().mapToInt(OrderLine::getAmount).sum();
        int lineCount = lines.size();
        if (lines.isEmpty()) {
            return Stream.empty();
        }
        return lines.stream().map(line -> new OrderListItemResponse(
            h.getId(),
            h.getOrderNumber(),
            h.getContractPartyCode(),
            nameByCode.get(h.getContractPartyCode()),
            h.getDeliveryPartyCode(),
            nameByCode.get(h.getDeliveryPartyCode()),
            h.getDeliveryLocation(),
            h.getDueDate(),
            h.getForecastNumber(),
            totalAmount,
            lineCount,
            h.getCreatedAt(),
            line.getId(),
            line.getLineNo(),
            line.getProductCode(),
            line.getProductName(),
            line.getQuantity(),
            line.getUnitPrice(),
            line.getAmount()
        ));
    }

    @Transactional
    public OrderCreateResponse createOrder(OrderCreateRequest request) {
        if (request.lines() == null || request.lines().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "明細が1行以上必要です");
        }

        var header = new OrderHeader();
        header.setOrderNumber(nextOrderNumber());
        applyHeaderFields(header, request);
        applyLinesFromRequest(header, request.lines());

        var saved = orderHeaderRepository.save(header);
        return new OrderCreateResponse(
            saved.getId(),
            saved.getOrderNumber(),
            "受注を登録しました"
        );
    }

    @Transactional
    public OrderCreateResponse updateOrder(Long id, OrderCreateRequest request) {
        if (request.lines() == null || request.lines().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "明細が1行以上必要です");
        }

        OrderHeader header = orderHeaderRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "受注が見つかりません"));
        applyHeaderFields(header, request);
        applyLinesFromRequest(header, request.lines());

        var saved = orderHeaderRepository.save(header);
        return new OrderCreateResponse(
            saved.getId(),
            saved.getOrderNumber(),
            "受注を更新しました"
        );
    }

    private static void applyHeaderFields(OrderHeader header, OrderCreateRequest request) {
        header.setContractPartyCode(request.contractPartyCode().trim());
        header.setDeliveryPartyCode(request.deliveryPartyCode().trim());
        header.setDeliveryLocation(blankToNull(request.deliveryLocation()));
        header.setDueDate(request.dueDate());
        header.setForecastNumber(blankToNull(request.forecastNumber()));
    }

    /**
     * 明細をリクエスト内容で置き換える（既存明細は clear により削除され、新規行が追加される）。
     */
    private static void applyLinesFromRequest(OrderHeader header, List<OrderLineRequest> requests) {
        header.getLines().clear();
        int lineNo = 1;
        for (OrderLineRequest lineReq : requests) {
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
    }

    private static OrderDetailResponse toDetailResponse(OrderHeader h) {
        List<OrderDetailLineResponse> lines = h.getLines().stream()
            .sorted(Comparator.comparing(OrderLine::getLineNo))
            .map(l -> new OrderDetailLineResponse(
                l.getId(),
                l.getLineNo(),
                l.getProductCode(),
                l.getProductName(),
                l.getQuantity(),
                l.getUnitPrice(),
                l.getAmount()
            ))
            .toList();
        return new OrderDetailResponse(
            h.getId(),
            h.getOrderNumber(),
            h.getContractPartyCode(),
            h.getDeliveryPartyCode(),
            h.getDeliveryLocation(),
            h.getDueDate(),
            h.getForecastNumber(),
            lines
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
