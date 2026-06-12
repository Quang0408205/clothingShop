package com.example.clothingshop.repository;

import com.example.clothingshop.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

    // Nhom cha (parent_id IS NULL)
    List<Category> findByParentIdIsNullOrderById();

    // Danh muc con (la, dung de gan san pham)
    List<Category> findByParentIdIsNotNullOrderById();

    // Cac danh muc con cua mot nhom cha
    List<Category> findByParentIdOrderById(Integer parentId);

    boolean existsBySlug(String slug);

    List<Category> findByParentIdIsNullAndNameContainingIgnoreCaseOrderById(String name);

    List<Category> findByParentIdIsNotNullAndNameContainingIgnoreCaseOrderById(String name);
}
