package com.hackathon.wishlist.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

/**
 * 위시템(사고 싶은 옷) 엔티티.
 * User : WishItem = 1 : N
 */
@Entity
@Table(name = "wish_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Long price;

    /** 쇼핑몰명 */
    private String shopName;

    /** 카테고리: 화면 select 고정값(상의/하의/아우터/신발/가방/액세서리) */
    @Column(nullable = false)
    private String category;

    /** 우선순위 1~5 (1이 가장 높음) */
    @Column(nullable = false)
    private Integer priority;

    /** 상품 URL (선택) */
    private String productUrl;

    /** 썸네일 URL (선택) */
    private String thumbnailUrl;

    /** 구매완료 여부 */
    @Builder.Default
    @Column(nullable = false)
    private boolean purchased = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 위시템 소유자 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
