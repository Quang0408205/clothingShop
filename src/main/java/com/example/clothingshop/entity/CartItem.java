package com.example.clothingshop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "cart_items")
@Getter
@Setter
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "product_id", nullable = false)
    private Integer productId;

    @Column(length = 10)
    private String size;            // kich co da chon (S/M/L/XL)

    @Column(nullable = false)
    private Integer quantity = 1;
}
