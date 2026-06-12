package com.example.clothingshop.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductDto(
        Integer id,
        String name,
        String description,
        BigDecimal price,
        Integer stock,
        String category,
        List<String> images
) {
}
