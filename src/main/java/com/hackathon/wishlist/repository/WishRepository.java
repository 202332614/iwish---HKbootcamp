package com.hackathon.wishlist.repository;

import com.hackathon.wishlist.domain.WishItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WishRepository extends JpaRepository<WishItem, Long> {

    /** 사용자별 위시리스트 (우선순위 오름차순, 같으면 최신순) */
    List<WishItem> findByUserIdOrderByPriorityAscCreatedAtDesc(Long userId);

    /** 전체 위시템 (관리자용, 최신순) */
    List<WishItem> findAllByOrderByCreatedAtDesc();

    /**
     * 사용자별 위시템 검색/필터.
     * 각 파라미터가 null 이면 해당 조건은 무시(=전체).
     * 정렬은 기본 목록과 동일(우선순위 오름차순, 같으면 최신순).
     */
    @Query("SELECT w FROM WishItem w WHERE w.user.id = :userId "
            + "AND (:q IS NULL OR LOWER(w.name) LIKE LOWER(CONCAT('%', :q, '%')) "
            + "         OR LOWER(w.shopName) LIKE LOWER(CONCAT('%', :q, '%'))) "
            + "AND (:category IS NULL OR w.category = :category) "
            + "AND (:minPrice IS NULL OR w.price >= :minPrice) "
            + "AND (:maxPrice IS NULL OR w.price <= :maxPrice) "
            + "AND (:priority IS NULL OR w.priority = :priority) "
            + "AND (:purchased IS NULL OR w.purchased = :purchased) "
            + "ORDER BY w.priority ASC, w.createdAt DESC")
    List<WishItem> search(@Param("userId") Long userId,
                          @Param("q") String q,
                          @Param("category") String category,
                          @Param("minPrice") Long minPrice,
                          @Param("maxPrice") Long maxPrice,
                          @Param("priority") Integer priority,
                          @Param("purchased") Boolean purchased);

    /**
     * 사용자별 미구매 위시 총액.
     * 결과가 없으면 SUM 은 null 이므로 COALESCE 로 0 보장 (null 방지).
     */
    @Query("SELECT COALESCE(SUM(w.price), 0) FROM WishItem w "
            + "WHERE w.user.id = :userId AND w.purchased = false")
    long sumUnpurchasedPriceByUserId(@Param("userId") Long userId);
}
