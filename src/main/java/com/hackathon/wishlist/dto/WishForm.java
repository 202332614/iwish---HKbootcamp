package com.hackathon.wishlist.dto;

import com.hackathon.wishlist.domain.WishItem;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 위시템 등록/수정 공용 폼 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
public class WishForm {

    @NotBlank(message = "상품명을 입력하세요.")
    private String name;

    @NotNull(message = "가격을 입력하세요.")
    @Positive(message = "가격은 0보다 커야 합니다.")
    private Long price;

    /** 쇼핑몰명 (선택) */
    private String shopName;

    @NotBlank(message = "카테고리를 선택하세요.")
    private String category;

    @NotNull(message = "우선순위를 선택하세요.")
    @Min(value = 1, message = "우선순위는 1~5입니다.")
    @Max(value = 5, message = "우선순위는 1~5입니다.")
    private Integer priority;

    /** 상품 URL (선택) */
    private String productUrl;

    /** 썸네일 URL (선택) */
    private String thumbnailUrl;

    /** 수정 화면 진입 시 기존 엔티티 값으로 폼을 채운다. */
    public static WishForm from(WishItem item) {
        WishForm form = new WishForm();
        form.name = item.getName();
        form.price = item.getPrice();
        form.shopName = item.getShopName();
        form.category = item.getCategory();
        form.priority = item.getPriority();
        form.productUrl = item.getProductUrl();
        form.thumbnailUrl = item.getThumbnailUrl();
        return form;
    }
}
