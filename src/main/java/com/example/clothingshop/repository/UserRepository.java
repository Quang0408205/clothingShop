package com.example.clothingshop.repository;

import com.example.clothingshop.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<User> findAllByOrderByIdDesc(Pageable pageable);

    Page<User> findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrderByIdDesc(
            String name, String email, Pageable pageable);
}
