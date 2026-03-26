package com.example.demo.api.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProjectCreateRequest(
    @NotBlank
    @Size(max = 100)
    String name,
    @Size(max = 500)
    String description
) {}
