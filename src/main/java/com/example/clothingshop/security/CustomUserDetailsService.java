package com.example.clothingshop.security;

import com.example.clothingshop.entity.User;
import com.example.clothingshop.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Primary
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User u = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Khong tim thay tai khoan: " + email));

        return org.springframework.security.core.userdetails.User.builder()
                .username(u.getEmail())
                .password(u.getPassword())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole())))
                .disabled(Boolean.FALSE.equals(u.getEnabled()))
                .build();
    }
}
