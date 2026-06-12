package com.example.clothingshop.controller.user;

import com.example.clothingshop.dto.RegisterForm;
import com.example.clothingshop.entity.User;
import com.example.clothingshop.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String login() {
        return "user/login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("form", new RegisterForm());
        return "user/register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute("form") RegisterForm form, Model model) {
        if (form.getEmail() == null || form.getEmail().isBlank()
                || form.getPassword() == null || form.getPassword().length() < 6
                || form.getFullName() == null || form.getFullName().isBlank()) {
            model.addAttribute("error", "Vui lòng nhập đủ thông tin, mật khẩu tối thiểu 6 ký tự.");
            return "user/register";
        }
        if (userRepository.existsByEmail(form.getEmail())) {
            model.addAttribute("error", "Email này đã được đăng ký.");
            return "user/register";
        }

        User u = new User();
        u.setFullName(form.getFullName().trim());
        u.setEmail(form.getEmail().trim().toLowerCase());
        u.setPassword(passwordEncoder.encode(form.getPassword()));   // ma hoa BCrypt
        u.setPhone(form.getPhone());
        u.setRole("CUSTOMER");
        u.setEnabled(true);
        userRepository.save(u);

        return "redirect:/login?registered";
    }
}
