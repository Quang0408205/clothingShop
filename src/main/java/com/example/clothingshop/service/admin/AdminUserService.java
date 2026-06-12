package com.example.clothingshop.service.admin;

import com.example.clothingshop.entity.User;
import com.example.clothingshop.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class AdminUserService {

    private final UserRepository userRepository;

    public AdminUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Page<User> listUsers(int page, int size) {
        return userRepository.findAllByOrderByIdDesc(PageRequest.of(page, size));
    }

    public Page<User> searchUsers(String q, int page, int size) {
        return userRepository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrderByIdDesc(
                q, q, PageRequest.of(page, size));
    }
}
