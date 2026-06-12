package com.example.clothingshop.controller.admin;

import com.example.clothingshop.entity.User;
import com.example.clothingshop.repository.UserRepository;
import com.example.clothingshop.service.admin.AdminUserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    private final AdminUserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserController(AdminUserService userService,
                               UserRepository userRepository,
                               PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String q,
                       @RequestParam(defaultValue = "0") int page, Model model) {
        var users = q.isBlank() ? userService.listUsers(page, 20)
                                : userService.searchUsers(q.trim(), page, 20);
        model.addAttribute("users", users);
        model.addAttribute("q", q);
        return "admin/users";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model, RedirectAttributes ra) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            ra.addFlashAttribute("error", "Không tìm thấy người dùng.");
            return "redirect:/admin/users";
        }
        model.addAttribute("user", user);
        return "admin/user-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute User formUser,
                       @RequestParam(value = "newPassword", required = false) String newPassword,
                       RedirectAttributes ra) {
        User existing = userRepository.findById(formUser.getId()).orElse(null);
        if (existing == null) {
            ra.addFlashAttribute("error", "Không tìm thấy người dùng.");
            return "redirect:/admin/users";
        }

        existing.setFullName(formUser.getFullName());
        existing.setPhone(formUser.getPhone());
        existing.setRole(formUser.getRole());
        existing.setEnabled(formUser.getEnabled());

        if (newPassword != null && !newPassword.isBlank()) {
            existing.setPassword(passwordEncoder.encode(newPassword));
        }

        userRepository.save(existing);
        ra.addFlashAttribute("success", "Đã cập nhật người dùng " + existing.getEmail());
        return "redirect:/admin/users";
    }

    @PostMapping("/toggle/{id}")
    public String toggle(@PathVariable Integer id, RedirectAttributes ra) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            ra.addFlashAttribute("error", "Không tìm thấy người dùng.");
            return "redirect:/admin/users";
        }
        user.setEnabled(!user.getEnabled());
        userRepository.save(user);
        ra.addFlashAttribute("success",
            user.getEnabled() ? "Đã kích hoạt tài khoản " + user.getEmail()
                              : "Đã khoá tài khoản " + user.getEmail());
        return "redirect:/admin/users";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            ra.addFlashAttribute("error", "Không tìm thấy người dùng.");
            return "redirect:/admin/users";
        }
        if ("ADMIN".equals(user.getRole())) {
            ra.addFlashAttribute("error", "Không thể xoá tài khoản Admin.");
            return "redirect:/admin/users";
        }
        userRepository.deleteById(id);
        ra.addFlashAttribute("success", "Đã xoá tài khoản " + user.getEmail());
        return "redirect:/admin/users";
    }
}