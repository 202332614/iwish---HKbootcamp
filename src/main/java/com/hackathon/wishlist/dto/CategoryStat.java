package com.hackathon.wishlist.dto;

import lombok.Getter;

/** 카테고리별 통계(금액 비중). */
@Getter
public class CategoryStat {
    private final String category;
    private final int count;
    private final long amount;
    /** 전체 금액 대비 비중(%) */
    private final int percent;

    public CategoryStat(String category, int count, long amount, int percent) {
        this.category = category;
        this.count = count;
        this.amount = amount;
        this.percent = percent;
    }
}
