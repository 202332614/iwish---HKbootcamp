package com.hackathon.wishlist.controller;

import com.hackathon.wishlist.service.StatsService;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 통계 대시보드 화면.
 * /stats 는 SecurityConfig 의 anyRequest().authenticated() 로 보호됨.
 */
@Controller
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/stats")
    public String stats(Principal principal, Model model) {
        model.addAttribute("stats", statsService.getStats(principal.getName()));
        return "stats";
    }
}
