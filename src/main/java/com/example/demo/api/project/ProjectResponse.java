package com.example.demo.api.project;

public record ProjectResponse(
    Long id,
    Long version, // version を追加
    String name,
    String description
) {}
