package com.example.demo.api.master;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.repository.PartyRepository;
import com.example.demo.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/masters")
@RequiredArgsConstructor
public class MasterController {

    private final PartyRepository partyRepository;
    private final ProductRepository productRepository;

    @GetMapping("/parties")
    public List<CodeNameResponse> parties() {
        return partyRepository.findAllByOrderByCodeAsc().stream()
            .map(p -> new CodeNameResponse(p.getCode(), p.getName()))
            .toList();
    }

    @GetMapping("/products")
    public List<CodeNameResponse> products() {
        return productRepository.findAllByOrderByCodeAsc().stream()
            .map(p -> new CodeNameResponse(p.getCode(), p.getName()))
            .toList();
    }
}
