package com.example.clothingshop.controller;

import com.example.clothingshop.dto.CategoryGroup;
import com.example.clothingshop.entity.Category;
import com.example.clothingshop.repository.CartItemRepository;
import com.example.clothingshop.repository.CategoryRepository;
import com.example.clothingshop.repository.UserRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Comparator;
import java.util.List;

// Bien dung chung cho moi trang (header): so luong gio hang + menu danh muc
@ControllerAdvice
public class GlobalModelAttributes {

    private final UserRepository userRepository;
    private final CartItemRepository cartRepository;
    private final CategoryRepository categoryRepository;

    public GlobalModelAttributes(UserRepository userRepository, CartItemRepository cartRepository,
                                 CategoryRepository categoryRepository) {
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
        this.categoryRepository = categoryRepository;
    }

    @ModelAttribute("cartCount")
    public long cartCount(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return 0;
        }
        return userRepository.findByEmail(auth.getName())
                .map(u -> cartRepository.countByUserId(u.getId()))
                .orElse(0L);
    }

    // Menu danh muc cho header (nhom cha + danh muc con) - dung tren moi trang
    @ModelAttribute("groups")
    public List<CategoryGroup> groups() {
        List<Category> all = categoryRepository.findAll();
        return all.stream()
                .filter(c -> c.getParentId() == null)
                .sorted(Comparator.comparing(Category::getId))
                .map(parent -> new CategoryGroup(parent,
                        all.stream()
                                .filter(c -> parent.getId().equals(c.getParentId()))
                                .sorted(Comparator.comparing(Category::getId))
                                .toList()))
                .toList();
    }
}
