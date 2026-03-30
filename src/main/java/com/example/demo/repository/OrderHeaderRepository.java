package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.OrderHeader;

public interface OrderHeaderRepository extends JpaRepository<OrderHeader, Long> {

    long countByOrderNumberStartingWith(String prefix);
}
