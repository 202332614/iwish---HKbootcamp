package com.hackathon.wishlist.controller;

import com.hackathon.wishlist.dto.CollectionForm;
import com.hackathon.wishlist.service.CollectionService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 룩북(코디 모음집) 화면/처리.
 * /collections/** 는 SecurityConfig 의 anyRequest().authenticated() 로 보호됨.
 * 소유자 검증은 CollectionService 에서 수행.
 */
@Controller
@RequiredArgsConstructor
public class CollectionController {

    /** 코디 느낌/스타일 고정 목록 */
    private static final List<String> STYLE_TAGS =
            List.of("미니멀", "캐주얼", "스트릿", "포멀", "러블리", "스포티", "빈티지");

    private final CollectionService collectionService;

    /* ===================== 목록 ===================== */

    @GetMapping("/collections")
    public String list(Principal principal, Model model) {
        model.addAttribute("collections", collectionService.getMyCollectionCards(principal.getName()));
        return "collections";
    }

    /* ===================== 생성 ===================== */

    @GetMapping("/collections/new")
    public String newForm(Model model) {
        model.addAttribute("collectionForm", new CollectionForm());
        model.addAttribute("styleTags", STYLE_TAGS);
        model.addAttribute("actionUrl", "/collections");
        model.addAttribute("editing", false);
        return "collection-form";
    }

    @PostMapping("/collections")
    public String create(@Valid @ModelAttribute("collectionForm") CollectionForm collectionForm,
                         BindingResult bindingResult,
                         Principal principal,
                         Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("styleTags", STYLE_TAGS);
            model.addAttribute("actionUrl", "/collections");
            model.addAttribute("editing", false);
            return "collection-form";
        }
        Long id = collectionService.create(principal.getName(), collectionForm);
        return "redirect:/collections/" + id;
    }

    /* ===================== 상세 ===================== */

    @GetMapping("/collections/{id}")
    public String detail(@PathVariable Long id, Principal principal, Model model) {
        String username = principal.getName();
        model.addAttribute("collection", collectionService.getMyCollection(username, id));
        model.addAttribute("totalPrice", collectionService.getTotalPrice(username, id));
        model.addAttribute("addableWishes", collectionService.getAddableWishes(username, id));
        return "collection-detail";
    }

    /* ===================== 수정 ===================== */

    @GetMapping("/collections/{id}/edit")
    public String editForm(@PathVariable Long id, Principal principal, Model model) {
        model.addAttribute("collectionForm",
                CollectionForm.from(collectionService.getMyCollection(principal.getName(), id)));
        model.addAttribute("styleTags", STYLE_TAGS);
        model.addAttribute("actionUrl", "/collections/" + id);
        model.addAttribute("editing", true);
        return "collection-form";
    }

    @PostMapping("/collections/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("collectionForm") CollectionForm collectionForm,
                         BindingResult bindingResult,
                         Principal principal,
                         Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("styleTags", STYLE_TAGS);
            model.addAttribute("actionUrl", "/collections/" + id);
            model.addAttribute("editing", true);
            return "collection-form";
        }
        collectionService.update(principal.getName(), id, collectionForm);
        return "redirect:/collections/" + id;
    }

    /* ===================== 삭제 ===================== */

    @PostMapping("/collections/{id}/delete")
    public String delete(@PathVariable Long id, Principal principal) {
        collectionService.delete(principal.getName(), id);
        return "redirect:/collections";
    }

    /* ===================== 위시템 담기/빼기 ===================== */

    @PostMapping("/collections/{id}/items")
    public String addItem(@PathVariable Long id,
                          @RequestParam Long wishId,
                          Principal principal) {
        collectionService.addItem(principal.getName(), id, wishId);
        return "redirect:/collections/" + id;
    }

    @PostMapping("/collections/{id}/items/{wishId}/delete")
    public String removeItem(@PathVariable Long id,
                             @PathVariable Long wishId,
                             Principal principal) {
        collectionService.removeItem(principal.getName(), id, wishId);
        return "redirect:/collections/" + id;
    }
}
