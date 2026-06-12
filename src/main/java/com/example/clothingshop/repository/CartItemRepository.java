package com.example.clothingshop.repository;

import com.example.clothingshop.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Integer> {

    List<CartItem> findByUserId(Integer userId);

    Optional<CartItem> findByUserIdAndProductId(Integer userId, Integer productId);

    Optional<CartItem> findByUserIdAndProductIdAndSize(Integer userId, Integer productId, String size);

    long countByUserId(Integer userId);

    void deleteByUserId(Integer userId);
}
