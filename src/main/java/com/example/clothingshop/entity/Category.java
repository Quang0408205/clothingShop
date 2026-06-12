package com.example.clothingshop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "categories")
@Getter
@Setter
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100, unique = true)
    private String slug;

    @Column(name = "parent_id")
    private Integer parentId;   // null = nhom cha; co gia tri = danh muc con

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
