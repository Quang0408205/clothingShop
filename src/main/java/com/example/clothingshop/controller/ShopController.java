package com.example.clothingshop.controller;

import com.example.clothingshop.dto.CategoryGroup;
import com.example.clothingshop.entity.Category;
import com.example.clothingshop.repository.CategoryRepository;
import com.example.clothingshop.repository.ProductRepository;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class ShopController {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public ShopController(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    // Trang chu: danh sach san pham
    @GetMapping("/")
    @Transactional(readOnly = true)
    public String index(@RequestParam(value = "categoryId", required = false) Integer categoryId, Model model) {
        // Menu phan cap: moi nhom cha + cac danh muc con
        List<CategoryGroup> groups = categoryRepository.findByParentIdIsNullOrderById().stream()
                .map(parent -> new CategoryGroup(parent,
                        categoryRepository.findByParentIdOrderById(parent.getId())))
                .toList();
        model.addAttribute("groups", groups);
        model.addAttribute("selectedCategoryId", categoryId);

        // Ten danh muc dang chon (de hien tieu de)
        String selectedName = null;
        if (categoryId != null) {
            selectedName = categoryRepository.findById(categoryId).map(Category::getName).orElse(null);
        }
        model.addAttribute("selectedCategoryName", selectedName);

        model.addAttribute(
                "products",
                categoryId == null
                        ? productRepository.findAllWithDetails()
                        : productRepository.findByCategoryIdWithDetails(categoryId)
        );

        return "index";
    }

    // Trang chi tiet 1 san pham
    @GetMapping("/products/{id}")
    @Transactional(readOnly = true)
    public String detail(@PathVariable Integer id, Model model, RedirectAttributes ra) {
        return productRepository.findByIdWithDetails(id)
                .map(p -> {
                    model.addAttribute("product", p);
                    return "product";
                })
                .orElse("redirect:/");
    }
}
