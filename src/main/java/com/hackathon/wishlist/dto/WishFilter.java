package com.hackathon.wishlist.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 위시리스트 검색/필터 조건 (쿼리 파라미터 바인딩).
 * 빈 값은 컨트롤러/서비스에서 null 로 정규화하여 "조건 없음"으로 처리한다.
 */
@Getter
@Setter
@NoArgsConstructor
public class WishFilter {

    /** 키워드 (상품명/쇼핑몰 부분일치) */
    private String q;

    /** 카테고리 (정확히 일치) */
    private String category;

    /** 최소 가격 */
    private Long minPrice;

    /** 최대 가격 */
    private Long maxPrice;

    /** 우선순위 1~5 */
    private Integer priority;

    /** 구매여부: null=전체, false=미구매, true=구매완료 */
    private Boolean purchased;

    /** 활성화된 필터가 하나라도 있는지 (빈 상태 메시지 분기용) */
    public boolean isActive() {
        return (q != null && !q.isBlank())
                || (category != null && !category.isBlank())
                || minPrice != null
                || maxPrice != null
                || priority != null
                || purchased != null;
    }
}
