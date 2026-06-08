package com.hackathon.wishlist.dto;

import lombok.Getter;

/** 우선순위별 분포. */
@Getter
public class PriorityStat {
    private final int priority;
    private final int count;
    /** 전체 개수 대비 비중(%) */
    private final int percent;

    public PriorityStat(int priority, int count, int percent) {
        this.priority = priority;
        this.count = count;
        this.percent = percent;
    }
}
