package com.example.clothingshop.security;

import com.example.clothingshop.entity.User;
import com.example.clothingshop.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collection;
import java.util.List;

public class JpaUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public JpaUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User u = userRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new org.springframework.security.core.userdetails.User(u.getEmail(), u.getPassword(), u.getEnabled(), true, true, true, mapRoles(u.getRole()));
    }

    private Collection<? extends GrantedAuthority> mapRoles(String role) {
        if (role == null) return List.of();
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }
}
