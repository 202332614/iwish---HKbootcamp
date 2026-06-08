package com.hackathon.wishlist.config;

import com.hackathon.wishlist.domain.Role;
import com.hackathon.wishlist.domain.User;
import com.hackathon.wishlist.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 앱 시작 시 초기 ADMIN 계정 시딩.
 * - username: admin / password: admin1234 (BCrypt) / role: ADMIN
 * - 이미 존재하면 생성하지 않음.
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.existsByUsername("admin")) {
            return;
        }
        User admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin1234"))
                .role(Role.ADMIN)
                .budget(0L)
                .build();
        userRepository.save(admin);
    }
}
