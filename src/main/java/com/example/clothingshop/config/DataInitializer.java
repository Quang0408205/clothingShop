package com.example.clothingshop.config;

import com.example.clothingshop.entity.User;
import com.example.clothingshop.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    // Tao san 2 tai khoan demo neu DB chua co
    @Bean
    public CommandLineRunner seedUsers(UserRepository userRepository, PasswordEncoder encoder) {
        return args -> {
            createOrUpdateDefaultUser(userRepository, encoder, "admin@huyrc.vn", "Quản trị viên", "admin123", "ADMIN");
            createOrUpdateDefaultUser(userRepository, encoder, "khach@huyrc.vn", "Khách Demo", "123456", "CUSTOMER");
        };
    }

    private void createOrUpdateDefaultUser(UserRepository repo, PasswordEncoder encoder,
                                           String email, String name, String rawPassword, String role) {
        String hashed = encoder.encode(rawPassword);   // ma hoa BCrypt
        repo.findByEmail(email).ifPresentOrElse(user -> {
            user.setFullName(name);
            user.setPassword(hashed);
            user.setRole(role);
            user.setEnabled(true);
            repo.save(user);
        }, () -> {
            User u = new User();
            u.setEmail(email);
            u.setFullName(name);
            u.setPassword(hashed);
            u.setRole(role);
            u.setEnabled(true);
            repo.save(u);
        });
    }
}
