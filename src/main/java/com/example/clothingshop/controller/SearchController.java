package com.example.clothingshop.controller;

import com.example.clothingshop.repository.ProductRepository;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class SearchController {

    private final ProductRepository productRepository;

    public SearchController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping("/search")
    @Transactional(readOnly = true)
    public String search(@RequestParam(required = false) String q, Model model) {
        String keyword = q == null ? "" : q.trim();
        model.addAttribute("keyword", keyword);
        model.addAttribute("products",
                keyword.isBlank() ? List.of() : productRepository.searchByName(keyword));
        return "search";
    }
}
