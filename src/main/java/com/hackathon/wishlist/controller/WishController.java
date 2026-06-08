package com.hackathon.wishlist.controller;

import com.hackathon.wishlist.dto.WishFilter;
import com.hackathon.wishlist.dto.WishForm;
import com.hackathon.wishlist.service.FileStorageService;
import com.hackathon.wishlist.service.WishService;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

/**
 * 위시리스트 CRUD + 예산 화면/처리.
 * 인증은 SecurityConfig 에서 보장(/wishlist/**, /budget 은 authenticated),
 * 소유자 검증은 WishService 에서 수행.
 */
@Controller
@RequiredArgsConstructor
public class WishController {

    /** wish-form.html 에서 사용할 카테고리 고정 목록 */
    private static final java.util.List<String> CATEGORIES =
            java.util.List.of("상의", "하의", "아우터", "신발", "가방", "액세서리");

    private final WishService wishService;
    private final FileStorageService fileStorageService;

    /* ===================== 목록 + 예산 대시보드 ===================== */

    @GetMapping("/wishlist")
    public String list(@ModelAttribute("filter") WishFilter filter,
                       Principal principal, Model model) {
        String username = principal.getName();
        model.addAttribute("wishes", wishService.getMyWishes(username, filter));
        model.addAttribute("summary", wishService.getBudgetSummary(username));
        model.addAttribute("categories", CATEGORIES);
        return "wishlist";
    }

    /* ===================== 등록 ===================== */

    @GetMapping("/wishlist/new")
    public String newForm(Model model) {
        model.addAttribute("wishForm", new WishForm());
        model.addAttribute("categories", CATEGORIES);
        model.addAttribute("actionUrl", "/wishlist");
        model.addAttribute("editing", false);
        return "wish-form";
    }

    @PostMapping("/wishlist")
    public String create(@Valid @ModelAttribute("wishForm") WishForm wishForm,
                         BindingResult bindingResult,
                         @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                         Principal principal,
                         Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", CATEGORIES);
            model.addAttribute("actionUrl", "/wishlist");
            model.addAttribute("editing", false);
            return "wish-form";
        }
        // 업로드한 이미지가 있으면 그것을 썸네일로 사용(직접 입력 URL/og 자동추출보다 우선).
        if (imageFile != null && !imageFile.isEmpty()) {
            wishForm.setThumbnailUrl(fileStorageService.store(imageFile));
        }
        wishService.create(principal.getName(), wishForm);
        return "redirect:/wishlist";
    }

    /* ===================== 수정 ===================== */

    @GetMapping("/wishlist/{id}/edit")
    public String editForm(@PathVariable Long id, Principal principal, Model model) {
        model.addAttribute("wishForm", WishForm.from(wishService.getMyWish(principal.getName(), id)));
        model.addAttribute("categories", CATEGORIES);
        model.addAttribute("actionUrl", "/wishlist/" + id);
        model.addAttribute("editing", true);
        return "wish-form";
    }

    @PostMapping("/wishlist/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("wishForm") WishForm wishForm,
                         BindingResult bindingResult,
                         @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                         Principal principal,
                         Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", CATEGORIES);
            model.addAttribute("actionUrl", "/wishlist/" + id);
            model.addAttribute("editing", true);
            return "wish-form";
        }
        if (imageFile != null && !imageFile.isEmpty()) {
            wishForm.setThumbnailUrl(fileStorageService.store(imageFile));
        }
        wishService.update(principal.getName(), id, wishForm);
        return "redirect:/wishlist";
    }

    /* ===================== 삭제 / 구매완료 토글 ===================== */

    @PostMapping("/wishlist/{id}/delete")
    public String delete(@PathVariable Long id, Principal principal) {
        wishService.delete(principal.getName(), id);
        return "redirect:/wishlist";
    }

    @PostMapping("/wishlist/{id}/toggle")
    public String togglePurchased(@PathVariable Long id, Principal principal) {
        wishService.togglePurchased(principal.getName(), id);
        return "redirect:/wishlist";
    }

    /* ===================== 예산 설정 ===================== */

    @PostMapping("/budget")
    public String updateBudget(@RequestParam Long budget, Principal principal) {
        wishService.updateBudget(principal.getName(), budget);
        return "redirect:/wishlist";
    }
}
