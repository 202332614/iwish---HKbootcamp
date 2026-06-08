package com.hackathon.wishlist.dto;

import lombok.Getter;

/**
 * 예산 대시보드 표시용 계산 결과.
 * 모든 계산은 Service 에서 수행하며 null 은 0 으로 처리(null 방지).
 */
@Getter
public class BudgetSummary {

    /** 설정된 월 예산 (미설정이면 0) */
    private final long budget;

    /** 미구매 위시템 총액 */
    private final long totalWishPrice;

    /** 예산 - 총액 (음수면 초과) */
    private final long remaining;

    /** 사용률(%) — 프로그레스바 표시용, 0~100 으로 캡 */
    private final int usagePercent;

    /** 캡 없는 실제 사용률(%) — 텍스트 표시용 */
    private final int rawUsagePercent;

    /** 예산 초과 여부 (예산이 설정된 경우에만 의미) */
    private final boolean over;

    /** 예산 설정 여부 */
    private final boolean budgetSet;

    public BudgetSummary(long budget, long totalWishPrice) {
        this.budget = budget;
        this.totalWishPrice = totalWishPrice;
        this.remaining = budget - totalWishPrice;
        this.budgetSet = budget > 0;

        if (budget > 0) {
            long raw = Math.round(totalWishPrice * 100.0 / budget);
            this.rawUsagePercent = (int) raw;
            this.usagePercent = (int) Math.min(100, raw);
            this.over = totalWishPrice > budget;
        } else {
            this.rawUsagePercent = 0;
            this.usagePercent = 0;
            this.over = false;
        }
    }
}
