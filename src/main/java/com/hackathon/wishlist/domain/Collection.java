package com.hackathon.wishlist.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

/**
 * 룩북(코디 모음집) 엔티티.
 * User : Collection = 1 : N, Collection : WishItem = N : M (단방향).
 * 단방향으로 둬서 기존 WishItem 엔티티는 수정하지 않는다.
 */
@Entity
@Table(name = "collections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Collection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    /** 설명 (선택) */
    @Column(length = 500)
    private String description;

    /** 코디 느낌/스타일 태그: 미니멀/캐주얼/스트릿/포멀/러블리/스포티/빈티지 */
    @Column(nullable = false)
    private String styleTag;

    /** 커버 이미지 URL (선택). 없으면 화면에서 첫 아이템 썸네일로 대체. */
    private String coverImageUrl;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 룩북 소유자 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 룩북에 담긴 위시템들 (단방향 N:M) */
    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "collection_wish_item",
            joinColumns = @JoinColumn(name = "collection_id"),
            inverseJoinColumns = @JoinColumn(name = "wish_item_id"))
    private Set<WishItem> items = new LinkedHashSet<>();
}
