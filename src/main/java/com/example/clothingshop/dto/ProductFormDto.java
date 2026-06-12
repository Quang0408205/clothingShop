package com.example.clothingshop.dto;

import java.math.BigDecimal;

public record ProductFormDto(
        Integer id,
        String name,
        String description,
        BigDecimal price,
        Integer stock,
        Integer categoryId
) {
}