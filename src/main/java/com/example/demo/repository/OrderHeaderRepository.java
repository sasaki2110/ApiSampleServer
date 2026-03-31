package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.example.demo.model.OrderHeader;

public interface OrderHeaderRepository extends JpaRepository<OrderHeader, Long>, JpaSpecificationExecutor<OrderHeader> {

    long countByOrderNumberStartingWith(String prefix);
}
