package com.example.demo.repository;

import java.time.LocalDate;

import org.springframework.data.jpa.domain.Specification;

import com.example.demo.model.OrderHeader;

import jakarta.persistence.criteria.JoinType;

public final class OrderHeaderSpecifications {

    private OrderHeaderSpecifications() {}

    /** 明細をまとめて読み込み（一覧の金額合計・行数用）。count クエリでは fetch しない。 */
    public static Specification<OrderHeader> fetchLines() {
        return (root, query, cb) -> {
            Class<?> rt = query.getResultType();
            if (rt != Long.class && rt != long.class) {
                root.fetch("lines", JoinType.LEFT);
                query.distinct(true);
            }
            return cb.conjunction();
        };
    }

    public static Specification<OrderHeader> orderNumberContains(String orderNumber) {
        if (orderNumber == null || orderNumber.isBlank()) {
            return (root, query, cb) -> cb.conjunction();
        }
        String pattern = "%" + orderNumber.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("orderNumber")), pattern);
    }

    public static Specification<OrderHeader> contractPartyCodeEquals(String code) {
        if (code == null || code.isBlank()) {
            return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) -> cb.equal(root.get("contractPartyCode"), code.trim());
    }

    /** 納期下限（納期未設定の行は対象外）。 */
    public static Specification<OrderHeader> dueDateFrom(LocalDate from) {
        if (from == null) {
            return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) -> cb.and(
            cb.isNotNull(root.get("dueDate")),
            cb.greaterThanOrEqualTo(root.get("dueDate"), from)
        );
    }

    /** 納期上限（納期未設定の行は対象外）。 */
    public static Specification<OrderHeader> dueDateTo(LocalDate to) {
        if (to == null) {
            return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) -> cb.and(
            cb.isNotNull(root.get("dueDate")),
            cb.lessThanOrEqualTo(root.get("dueDate"), to)
        );
    }
}
