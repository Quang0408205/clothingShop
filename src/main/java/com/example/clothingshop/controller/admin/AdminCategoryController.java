package com.example.clothingshop.controller.admin;

import com.example.clothingshop.entity.Category;
import com.example.clothingshop.repository.CategoryRepository;
import com.example.clothingshop.repository.ProductRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.Normalizer;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public AdminCategoryController(CategoryRepository categoryRepository,
                                   ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String q, Model model) {
        List<Category> parents  = q.isBlank()
                ? categoryRepository.findByParentIdIsNullOrderById()
                : categoryRepository.findByParentIdIsNullAndNameContainingIgnoreCaseOrderById(q.trim());
        List<Category> children = q.isBlank()
                ? categoryRepository.findByParentIdIsNotNullOrderById()
                : categoryRepository.findByParentIdIsNotNullAndNameContainingIgnoreCaseOrderById(q.trim());

        // Số danh mục con theo từng nhóm cha: parentId -> count
        Map<Integer, Long> childCount = children.stream()
                .collect(Collectors.groupingBy(Category::getParentId, Collectors.counting()));

        // Tên nhóm cha theo id: parentId -> name
        Map<Integer, String> parentName = parents.stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));

        model.addAttribute("parents",    parents);
        model.addAttribute("children",   children);
        model.addAttribute("childCount", childCount);
        model.addAttribute("parentName", parentName);
        model.addAttribute("q", q);
        return "admin/categories";
    }

    @GetMapping("/add")
    public String addForm(@RequestParam(defaultValue = "child") String type, Model model) {
        model.addAttribute("category", new Category());
        model.addAttribute("parents", categoryRepository.findByParentIdIsNullOrderById());
        model.addAttribute("isEdit", false);
        model.addAttribute("isParent", "parent".equals(type));
        return "admin/category-form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model, RedirectAttributes ra) {
        Category category = categoryRepository.findById(id).orElse(null);
        if (category == null) {
            ra.addFlashAttribute("error", "Không tìm thấy danh mục.");
            return "redirect:/admin/categories";
        }
        model.addAttribute("category", category);
        model.addAttribute("parents", categoryRepository.findByParentIdIsNullOrderById());
        model.addAttribute("isEdit", true);
        model.addAttribute("isParent", category.getParentId() == null);
        return "admin/category-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Category category,
                       @RequestParam(value = "parentIdStr", required = false) String parentIdStr,
                       RedirectAttributes ra) {
        category.setParentId(
            (parentIdStr == null || parentIdStr.isBlank()) ? null : Integer.parseInt(parentIdStr)
        );

        if (category.getName() == null || category.getName().isBlank()) {
            ra.addFlashAttribute("error", "Tên danh mục không được để trống.");
            return "redirect:/admin/categories";
        }

        // Tạo slug từ tên nếu chưa có hoặc đang tạo mới
        if (category.getSlug() == null || category.getSlug().isBlank()) {
            category.setSlug(generateSlug(category.getName()));
        }

        // Đảm bảo slug unique khi tạo mới
        if (category.getId() == null) {
            String baseSlug = category.getSlug();
            String slug = baseSlug;
            int idx = 1;
            while (categoryRepository.existsBySlug(slug)) {
                slug = baseSlug + "-" + idx++;
            }
            category.setSlug(slug);
        }

        categoryRepository.save(category);
        ra.addFlashAttribute("success", category.getId() == null ? "Đã thêm danh mục." : "Đã cập nhật danh mục.");
        return "redirect:/admin/categories";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        Category cat = categoryRepository.findById(id).orElse(null);
        if (cat == null) {
            ra.addFlashAttribute("error", "Không tìm thấy danh mục.");
            return "redirect:/admin/categories";
        }
        // Nhóm cha: còn danh mục con không?
        if (!categoryRepository.findByParentIdOrderById(id).isEmpty()) {
            ra.addFlashAttribute("error",
                "Không thể xoá nhóm \"" + cat.getName() + "\" vì đang có danh mục con. Xoá hết danh mục con trước.");
            return "redirect:/admin/categories";
        }
        // Danh mục con: còn sản phẩm không?
        if (productRepository.existsByCategoryId(id)) {
            ra.addFlashAttribute("error",
                "Không thể xoá danh mục \"" + cat.getName() + "\" vì đang có sản phẩm thuộc danh mục này. Chuyển hoặc xoá sản phẩm trước.");
            return "redirect:/admin/categories";
        }
        categoryRepository.deleteById(id);
        ra.addFlashAttribute("success", "Đã xoá \"" + cat.getName() + "\" thành công.");
        return "redirect:/admin/categories";
    }

    private String generateSlug(String name) {
        String normalized = Normalizer.normalize(name.toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replaceAll("đ", "d")
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-");
        return normalized;
    }
}