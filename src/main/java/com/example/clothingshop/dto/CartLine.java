package com.example.clothingshop.dto;

import com.example.clothingshop.entity.Product;

import java.math.BigDecimal;

// Mot dong trong gio hang: san pham + kich co + so luong + thanh tien
public record CartLine(Product product, String size, int quantity, BigDecimal subtotal) {
}
