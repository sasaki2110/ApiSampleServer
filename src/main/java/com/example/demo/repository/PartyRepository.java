package com.example.demo.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.Party;

public interface PartyRepository extends JpaRepository<Party, Long> {

    List<Party> findAllByOrderByCodeAsc();

    List<Party> findByCodeIn(Collection<String> codes);
}
