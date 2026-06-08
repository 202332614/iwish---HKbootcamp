package com.hackathon.wishlist.dto;

import lombok.Getter;

/**
 * 룩북 목록 화면용 요약 카드.
 * 합계/아이템 수/커버 결정은 Service 에서 계산해 담는다.
 */
@Getter
public class CollectionCard {

    private final Long id;
    private final String name;
    private final String styleTag;
    /** 커버 이미지(없으면 첫 아이템 썸네일, 그것도 없으면 null) */
    private final String coverImage;
    private final int itemCount;
    /** 이 룩북을 다 사면 드는 총액 */
    private final long totalPrice;

    public CollectionCard(Long id, String name, String styleTag,
                          String coverImage, int itemCount, long totalPrice) {
        this.id = id;
        this.name = name;
        this.styleTag = styleTag;
        this.coverImage = coverImage;
        this.itemCount = itemCount;
        this.totalPrice = totalPrice;
    }
}
