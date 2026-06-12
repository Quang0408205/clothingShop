package com.example.clothingshop.dto;

import com.example.clothingshop.entity.Category;

import java.util.List;

// Mot nhom cha + cac danh muc con (dung cho menu phan cap)
public record CategoryGroup(Category parent, List<Category> children) {
}
