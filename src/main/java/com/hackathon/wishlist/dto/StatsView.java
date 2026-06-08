package com.hackathon.wishlist.dto;

import java.util.List;
import lombok.Getter;

/**
 * 통계 대시보드 표시용 집계 결과. 모든 계산은 Service 에서 수행(null/0 방지).
 */
@Getter
public class StatsView {

    private final int totalCount;
    private final int purchasedCount;
    /** 구매 완료율(%) */
    private final int completionRate;

    private final long totalAmount;
    private final long purchasedAmount;

    /** 카테고리별 금액 비중 (금액 내림차순) */
    private final List<CategoryStat> categories;
    /** 우선순위 1~5 분포 */
    private final List<PriorityStat> priorities;

    public StatsView(int totalCount, int purchasedCount, int completionRate,
                     long totalAmount, long purchasedAmount,
                     List<CategoryStat> categories, List<PriorityStat> priorities) {
        this.totalCount = totalCount;
        this.purchasedCount = purchasedCount;
        this.completionRate = completionRate;
        this.totalAmount = totalAmount;
        this.purchasedAmount = purchasedAmount;
        this.categories = categories;
        this.priorities = priorities;
    }

    /** 위시템이 하나도 없는지 */
    public boolean isEmpty() {
        return totalCount == 0;
    }
}
