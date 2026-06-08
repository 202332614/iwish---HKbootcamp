package com.hackathon.wishlist.repository;

import com.hackathon.wishlist.domain.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CollectionRepository extends JpaRepository<Collection, Long> {

    /** 사용자별 룩북 (최신순) */
    List<Collection> findByUserIdOrderByCreatedAtDesc(Long userId);
}
