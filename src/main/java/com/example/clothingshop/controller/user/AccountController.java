package com.example.clothingshop.controller.user;

import com.example.clothingshop.entity.User;
import com.example.clothingshop.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/account")
public class AccountController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AccountController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Trang thong tin ca nhan
    @GetMapping
    @Transactional(readOnly = true)
    public String profile(Authentication auth, Model model) {
        model.addAttribute("user", currentUser(auth));
        return "user/profile";
    }

    // Cap nhat ho ten + so dien thoai
    @PostMapping("/update")
    @Transactional
    public String update(@RequestParam String fullName,
                         @RequestParam(required = false) String phone,
                         Authentication auth, RedirectAttributes ra) {
        User u = currentUser(auth);
        if (fullName != null && !fullName.isBlank()) {
            u.setFullName(fullName.trim());
        }
        u.setPhone(phone);
        userRepository.save(u);
        ra.addFlashAttribute("updated", true);
        return "redirect:/account";
    }

    @PostMapping("/change-password")
    @Transactional
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 Authentication auth, RedirectAttributes ra) {
        User u = currentUser(auth);
        if (!passwordEncoder.matches(currentPassword, u.getPassword())) {
            ra.addFlashAttribute("pwdError", "Mật khẩu hiện tại không đúng.");
            return "redirect:/account";
        }
        if (!newPassword.equals(confirmPassword)) {
            ra.addFlashAttribute("pwdError", "Mật khẩu mới không khớp.");
            return "redirect:/account";
        }
        if (newPassword.length() < 6) {
            ra.addFlashAttribute("pwdError", "Mật khẩu mới phải có ít nhất 6 ký tự.");
            return "redirect:/account";
        }
        u.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(u);
        ra.addFlashAttribute("pwdSuccess", "Đổi mật khẩu thành công.");
        return "redirect:/account";
    }

    private User currentUser(Authentication auth) {
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalStateException("Khong tim thay tai khoan dang nhap"));
    }
}
