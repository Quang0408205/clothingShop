package com.example.clothingshop.repository;

import com.example.clothingshop.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductImageRepository
        extends JpaRepository<ProductImage, Integer> {

    List<ProductImage> findByProductId(Integer productId);

    void deleteByProductId(Integer productId);
    int countByProductId(Integer productId);
}