package com.example.clothingshop.controller.admin;

import com.example.clothingshop.dto.ProductFormDto;
import com.example.clothingshop.entity.Category;
import com.example.clothingshop.entity.Product;
import com.example.clothingshop.entity.ProductImage;
import com.example.clothingshop.repository.CategoryRepository;
import com.example.clothingshop.repository.ProductImageRepository;
import com.example.clothingshop.repository.ProductRepository;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.util.Locale;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {
    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public AdminProductController(
            ProductImageRepository productImageRepository,
            ProductRepository productRepository,
            CategoryRepository categoryRepository) {

        this.productImageRepository = productImageRepository;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String q,
                       @RequestParam(defaultValue = "0") int page, Model model) {
        var products = q.isBlank()
                ? productRepository.findAllWithCategoryPaged(PageRequest.of(page, 20))
                : productRepository.searchWithCategoryPaged(q.trim(), PageRequest.of(page, 20));
        model.addAttribute("products", products);
        model.addAttribute("q", q);
        return "admin/products";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        ProductFormDto productForm = new ProductFormDto(
                null,
                "",
                "",
                null,
                0,
                null
        );

        model.addAttribute("productForm", productForm);
        model.addAttribute("categories", categoryRepository.findByParentIdIsNotNullOrderById());

        return "admin/product-form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(
            @PathVariable Integer id,
            Model model) {

        Product product = productRepository
                .findByIdWithDetails(id)
                .orElseThrow();

        ProductFormDto dto = new ProductFormDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getCategory().getId()
        );

        model.addAttribute("productForm", dto);
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryRepository.findByParentIdIsNotNullOrderById());

        return "admin/product-form";
    }

    @PostMapping("/save")
    public String save(
            @ModelAttribute ProductFormDto form,
            @RequestParam(value = "thumbnailFile", required = false) MultipartFile thumbnailFile,
            @RequestParam(value = "files", required = false) MultipartFile[] files,
            Model model) {

        try {
            if (form.categoryId() == null) {
                throw new IllegalArgumentException("Vui long chon danh muc");
            }

            Product product = (form.id() != null)
                    ? productRepository.findById(form.id()).orElseThrow()
                    : new Product();

            Category category = categoryRepository.findById(form.categoryId()).orElseThrow();
            String folder = category.getSlug();

            product.setName(form.name());
            product.setDescription(form.description());
            product.setPrice(form.price());
            product.setStock(form.stock());
            product.setCategory(category);

            if (product.getSlug() == null || product.getSlug().isBlank()) {
                product.setSlug(generateSlug(form.name()));
            }

            if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
                product.setThumbnail(saveImage(thumbnailFile, folder));
            }

            product = productRepository.save(product);

            if (files != null && files.length > 0) {
                int sortOrder = productImageRepository.countByProductId(product.getId()) + 1;

                for (MultipartFile file : files) {
                    if (file.isEmpty()) {
                        continue;
                    }

                    ProductImage image = new ProductImage();
                    image.setProduct(product);
                    image.setSortOrder(sortOrder++);
                    image.setImagePath(saveImage(file, folder));

                    productImageRepository.save(image);
                }
            }

            return "redirect:/admin/products";
        } catch (IllegalArgumentException | IOException ex) {
            return productFormWithError(form, model, ex.getMessage());
        }
    }

    private String generateSlug(String name) {
        String baseSlug = Normalizer.normalize(name, Normalizer.Form.NFD)
                .replaceAll("\\u0111", "d")
                .replaceAll("\\u0110", "D")
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");

        if (baseSlug.isBlank()) {
            baseSlug = "san-pham";
        }

        return baseSlug + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String saveImage(MultipartFile file, String folder) throws IOException {
        try (InputStream inputStream = file.getInputStream()) {
            if (ImageIO.read(inputStream) == null) {
                throw new IllegalArgumentException("File upload khong phai hinh anh hop le");
            }
        }

        Path uploadDir = Paths.get("src/main/resources/static/images", folder);

        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = uploadDir.resolve(fileName);

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return "images/" + folder + "/" + fileName;
    }

    private String productFormWithError(ProductFormDto form, Model model, String message) {
        model.addAttribute("productForm", form);
        model.addAttribute("categories", categoryRepository.findByParentIdIsNotNullOrderById());
        model.addAttribute("uploadError", message);

        if (form.id() != null) {
            productRepository.findByIdWithDetails(form.id())
                    .ifPresent(product -> model.addAttribute("product", product));
        }

        return "admin/product-form";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        productRepository.deleteById(id);

        return "redirect:/admin/products";
    }
}
