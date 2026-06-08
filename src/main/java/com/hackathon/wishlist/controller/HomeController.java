package com.hackathon.wishlist.controller;

import com.hackathon.wishlist.dto.SignupDto;
import com.hackathon.wishlist.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * 랜딩 / 회원가입 / 로그인 화면 컨트롤러.
 * - 로그인 "처리"는 Spring Security 가 담당하므로 여기서는 화면(GET)만 제공.
 */
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final UserService userService;

    /** 랜딩 페이지 */
    @GetMapping("/")
    public String index() {
        return "index";
    }

    /** 회원가입 화면 */
    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("signupDto", new SignupDto());
        return "signup";
    }

    /** 회원가입 처리 */
    @PostMapping("/signup")
    public String signup(@Valid @ModelAttribute("signupDto") SignupDto signupDto,
                         BindingResult bindingResult,
                         Model model) {
        if (bindingResult.hasErrors()) {
            return "signup";
        }
        try {
            userService.signup(signupDto);
        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("username", "duplicate", e.getMessage());
            return "signup";
        }
        return "redirect:/login?signup";
    }

    /** 로그인 화면 (처리는 Spring Security) */
    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }
}
