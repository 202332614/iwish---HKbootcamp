package com.hackathon.wishlist.service;

import com.hackathon.wishlist.domain.Role;
import com.hackathon.wishlist.domain.User;
import com.hackathon.wishlist.dto.SignupDto;
import com.hackathon.wishlist.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 회원 관련 비즈니스 로직.
 * 비밀번호 암호화 / 기본값(role=USER, budget=0) 설정은 Controller 가 아닌 여기서 처리.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /** 회원가입. 신규 가입자는 role=USER, budget=0 으로 초기화. */
    @Transactional
    public void signup(SignupDto dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        User user = User.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(Role.USER)
                .budget(0L)
                .build();

        userRepository.save(user);
    }
}
