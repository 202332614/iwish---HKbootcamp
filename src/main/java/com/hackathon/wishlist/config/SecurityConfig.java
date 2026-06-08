package com.hackathon.wishlist.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // 정적 리소스 허용 (CSS/JS 차단 방지)
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/uploads/**").permitAll()
                        // 랜딩/회원가입/로그인 허용
                        .requestMatchers("/", "/signup", "/login").permitAll()
                        // 로그인 사용자 공통 경로 (소유자 검증은 Service 에서)
                        .requestMatchers("/wishlist/**", "/budget").authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/wishlist", true)
                        .failureUrl("/login?error")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        // 로그아웃 시 remember-me 쿠키도 제거
                        .deleteCookies("remember-me")
                        .permitAll()
                )
                // 로그인 유지: 해시 토큰 기반 쿠키(DB 불필요). 세션/앱 재시작 후에도 자동 재인증.
                .rememberMe(remember -> remember
                        // 토큰 서명 키. 운영에선 외부 설정/환경변수로 빼는 것을 권장.
                        .key("wishlist-remember-me-key")
                        // 로그인 폼의 체크박스 name 과 일치
                        .rememberMeParameter("remember-me")
                        // 유효기간 14일
                        .tokenValiditySeconds(60 * 60 * 24 * 14)
                );
        // CSRF 는 기본 활성화 유지. Thymeleaf 폼(th:action)은 토큰 자동 포함.
        return http.build();
    }
}
