package com.hackathon.wishlist.service;

import com.hackathon.wishlist.domain.User;
import com.hackathon.wishlist.domain.WishItem;
import com.hackathon.wishlist.dto.CategoryStat;
import com.hackathon.wishlist.dto.PriorityStat;
import com.hackathon.wishlist.dto.StatsView;
import com.hackathon.wishlist.repository.UserRepository;
import com.hackathon.wishlist.repository.WishRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * 통계 대시보드 집계.
 * 본인 위시템 전체를 기준으로 카테고리별 금액 비중 / 구매 완료율 / 우선순위 분포를 계산.
 * 합계·비율 계산은 모두 여기서 수행하며 0으로 나누지 않도록 방어한다.
 */
@Service
@RequiredArgsConstructor
public class StatsService {

    private final WishRepository wishRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public StatsView getStats(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."));

        List<WishItem> wishes =
                wishRepository.findByUserIdOrderByPriorityAscCreatedAtDesc(user.getId());

        int totalCount = wishes.size();
        int purchasedCount = 0;
        long totalAmount = 0L;
        long purchasedAmount = 0L;

        // 카테고리별 금액/개수 (입력 순서 유지)
        Map<String, long[]> byCategory = new LinkedHashMap<>(); // [count, amount]
        // 우선순위 1~5 개수
        int[] priorityCount = new int[6]; // index 1..5

        for (WishItem w : wishes) {
            long price = w.getPrice() == null ? 0L : w.getPrice();
            totalAmount += price;
            if (w.isPurchased()) {
                purchasedCount++;
                purchasedAmount += price;
            }
            byCategory.computeIfAbsent(w.getCategory(), k -> new long[2]);
            long[] agg = byCategory.get(w.getCategory());
            agg[0] += 1;
            agg[1] += price;

            int p = w.getPriority() == null ? 0 : w.getPriority();
            if (p >= 1 && p <= 5) {
                priorityCount[p]++;
            }
        }

        int completionRate = totalCount == 0 ? 0
                : (int) Math.round(purchasedCount * 100.0 / totalCount);

        // 카테고리 통계 → 금액 내림차순 정렬
        List<CategoryStat> categories = new ArrayList<>();
        for (Map.Entry<String, long[]> e : byCategory.entrySet()) {
            long amount = e.getValue()[1];
            int percent = totalAmount == 0 ? 0 : (int) Math.round(amount * 100.0 / totalAmount);
            categories.add(new CategoryStat(e.getKey(), (int) e.getValue()[0], amount, percent));
        }
        categories.sort((a, b) -> Long.compare(b.getAmount(), a.getAmount()));

        // 우선순위 분포 1~5
        List<PriorityStat> priorities = new ArrayList<>();
        for (int p = 1; p <= 5; p++) {
            int count = priorityCount[p];
            int percent = totalCount == 0 ? 0 : (int) Math.round(count * 100.0 / totalCount);
            priorities.add(new PriorityStat(p, count, percent));
        }

        return new StatsView(totalCount, purchasedCount, completionRate,
                totalAmount, purchasedAmount, categories, priorities);
    }
}
