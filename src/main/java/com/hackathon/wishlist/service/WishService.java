package com.hackathon.wishlist.service;

import com.hackathon.wishlist.domain.User;
import com.hackathon.wishlist.domain.WishItem;
import com.hackathon.wishlist.dto.BudgetSummary;
import com.hackathon.wishlist.dto.WishFilter;
import com.hackathon.wishlist.dto.WishForm;
import com.hackathon.wishlist.repository.UserRepository;
import com.hackathon.wishlist.repository.WishRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * 위시리스트 CRUD + 예산 비즈니스 로직.
 * - 모든 조회/변경은 "현재 로그인 사용자" 기준이며, 소유자 검증을 여기서 수행한다.
 * - 합계/예산 계산은 null 을 0 으로 처리한다(null 방지).
 */
@Service
@RequiredArgsConstructor
public class WishService {

    private final WishRepository wishRepository;
    private final UserRepository userRepository;
    private final OgImageExtractor ogImageExtractor;

    /* ===================== 조회 ===================== */

    /** 본인 위시리스트 (우선순위 오름차순, 같으면 최신순) */
    @Transactional(readOnly = true)
    public List<WishItem> getMyWishes(String username) {
        User user = getUser(username);
        return wishRepository.findByUserIdOrderByPriorityAscCreatedAtDesc(user.getId());
    }

    /** 본인 위시리스트 (검색/필터 적용). 빈 조건은 null 로 정규화해 무시. */
    @Transactional(readOnly = true)
    public List<WishItem> getMyWishes(String username, WishFilter filter) {
        User user = getUser(username);
        return wishRepository.search(
                user.getId(),
                emptyToNull(filter.getQ()),
                emptyToNull(filter.getCategory()),
                filter.getMinPrice(),
                filter.getMaxPrice(),
                filter.getPriority(),
                filter.getPurchased());
    }

    /** 본인 소유 위시템 단건. 없거나 소유자가 아니면 404. */
    @Transactional(readOnly = true)
    public WishItem getMyWish(String username, Long wishId) {
        User user = getUser(username);
        return getOwnedWish(user, wishId);
    }

    /** 예산 대시보드용 요약 (예산, 미구매 총액, 잔액, 사용률, 초과여부). */
    @Transactional(readOnly = true)
    public BudgetSummary getBudgetSummary(String username) {
        User user = getUser(username);
        long budget = user.getBudget() == null ? 0L : user.getBudget();
        long total = wishRepository.sumUnpurchasedPriceByUserId(user.getId());
        return new BudgetSummary(budget, total);
    }

    /* ===================== 생성/수정/삭제 ===================== */

    /** 위시템 등록 (현재 사용자 소유로). */
    @Transactional
    public void create(String username, WishForm form) {
        User user = getUser(username);
        String productUrl = emptyToNull(form.getProductUrl());
        WishItem item = WishItem.builder()
                .name(form.getName())
                .price(form.getPrice())
                .shopName(emptyToNull(form.getShopName()))
                .category(form.getCategory())
                .priority(form.getPriority())
                .productUrl(productUrl)
                .thumbnailUrl(resolveThumbnail(emptyToNull(form.getThumbnailUrl()), productUrl))
                .purchased(false)
                .user(user)
                .build();
        wishRepository.save(item);
    }

    /** 위시템 수정 (소유자만). */
    @Transactional
    public void update(String username, Long wishId, WishForm form) {
        User user = getUser(username);
        WishItem item = getOwnedWish(user, wishId);
        String productUrl = emptyToNull(form.getProductUrl());
        item.setName(form.getName());
        item.setPrice(form.getPrice());
        item.setShopName(emptyToNull(form.getShopName()));
        item.setCategory(form.getCategory());
        item.setPriority(form.getPriority());
        item.setProductUrl(productUrl);
        item.setThumbnailUrl(resolveThumbnail(emptyToNull(form.getThumbnailUrl()), productUrl));
        // 변경감지(dirty checking)로 flush
    }

    /** 위시템 삭제 (소유자만). */
    @Transactional
    public void delete(String username, Long wishId) {
        User user = getUser(username);
        WishItem item = getOwnedWish(user, wishId);
        wishRepository.delete(item);
    }

    /** 구매완료 토글 (소유자만). */
    @Transactional
    public void togglePurchased(String username, Long wishId) {
        User user = getUser(username);
        WishItem item = getOwnedWish(user, wishId);
        item.setPurchased(!item.isPurchased());
    }

    /* ===================== 예산 ===================== */

    /** 월 예산 설정. 음수/ null 은 0 으로 보정. */
    @Transactional
    public void updateBudget(String username, Long budget) {
        User user = getUser(username);
        long value = (budget == null || budget < 0) ? 0L : budget;
        user.setBudget(value);
        // 변경감지로 flush
    }

    /* ===================== 내부 헬퍼 ===================== */

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."));
    }

    /** 위시템을 조회하되, 현재 사용자 소유가 아니면 404(존재를 숨김). */
    private WishItem getOwnedWish(User user, Long wishId) {
        WishItem item = wishRepository.findById(wishId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "위시템을 찾을 수 없습니다."));
        if (!item.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "위시템을 찾을 수 없습니다.");
        }
        return item;
    }

    private String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    /**
     * 썸네일 결정 규칙:
     * - 사용자가 직접 입력한 thumbnailUrl 이 있으면 그것을 우선 사용.
     * - 없고 productUrl 이 있으면 og:image 를 자동 추출(실패 시 null).
     */
    private String resolveThumbnail(String thumbnailUrl, String productUrl) {
        if (thumbnailUrl != null) {
            return thumbnailUrl;
        }
        if (productUrl != null) {
            return ogImageExtractor.extractOgImage(productUrl);
        }
        return null;
    }
}
