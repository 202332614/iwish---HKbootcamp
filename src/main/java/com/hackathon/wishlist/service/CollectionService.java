package com.hackathon.wishlist.service;

import com.hackathon.wishlist.domain.Collection;
import com.hackathon.wishlist.domain.User;
import com.hackathon.wishlist.domain.WishItem;
import com.hackathon.wishlist.dto.CollectionCard;
import com.hackathon.wishlist.dto.CollectionForm;
import com.hackathon.wishlist.repository.CollectionRepository;
import com.hackathon.wishlist.repository.UserRepository;
import com.hackathon.wishlist.repository.WishRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * 룩북(코디 모음집) CRUD + 위시템 담기/빼기.
 * - 모든 조회/변경은 현재 로그인 사용자 기준, 소유자 검증은 여기서 수행.
 * - 합계/커버 결정/개수 계산은 Service 에서 처리(null 은 0 처리).
 */
@Service
@RequiredArgsConstructor
public class CollectionService {

    private final CollectionRepository collectionRepository;
    private final WishRepository wishRepository;
    private final UserRepository userRepository;

    /* ===================== 조회 ===================== */

    /** 본인 룩북 목록(요약 카드). */
    @Transactional(readOnly = true)
    public List<CollectionCard> getMyCollectionCards(String username) {
        User user = getUser(username);
        return collectionRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(c -> new CollectionCard(
                        c.getId(),
                        c.getName(),
                        c.getStyleTag(),
                        resolveCover(c),
                        c.getItems().size(),
                        totalPrice(c.getItems())))
                .collect(Collectors.toList());
    }

    /** 본인 소유 룩북 단건(상세). 없거나 소유자가 아니면 404. */
    @Transactional(readOnly = true)
    public Collection getMyCollection(String username, Long collectionId) {
        User user = getUser(username);
        return getOwnedCollection(user, collectionId);
    }

    /** 이 룩북을 다 사면 드는 총액. */
    @Transactional(readOnly = true)
    public long getTotalPrice(String username, Long collectionId) {
        User user = getUser(username);
        return totalPrice(getOwnedCollection(user, collectionId).getItems());
    }

    /** 아직 이 룩북에 담기지 않은 본인 위시템(담기 후보). */
    @Transactional(readOnly = true)
    public List<WishItem> getAddableWishes(String username, Long collectionId) {
        User user = getUser(username);
        Collection collection = getOwnedCollection(user, collectionId);
        Set<Long> contained = collection.getItems().stream()
                .map(WishItem::getId)
                .collect(Collectors.toSet());

        List<WishItem> result = new ArrayList<>();
        for (WishItem w : wishRepository.findByUserIdOrderByPriorityAscCreatedAtDesc(user.getId())) {
            if (!contained.contains(w.getId())) {
                result.add(w);
            }
        }
        return result;
    }

    /* ===================== 생성/수정/삭제 ===================== */

    @Transactional
    public Long create(String username, CollectionForm form) {
        User user = getUser(username);
        Collection collection = Collection.builder()
                .name(form.getName())
                .description(emptyToNull(form.getDescription()))
                .styleTag(form.getStyleTag())
                .coverImageUrl(emptyToNull(form.getCoverImageUrl()))
                .user(user)
                .build();
        return collectionRepository.save(collection).getId();
    }

    @Transactional
    public void update(String username, Long collectionId, CollectionForm form) {
        User user = getUser(username);
        Collection collection = getOwnedCollection(user, collectionId);
        collection.setName(form.getName());
        collection.setDescription(emptyToNull(form.getDescription()));
        collection.setStyleTag(form.getStyleTag());
        collection.setCoverImageUrl(emptyToNull(form.getCoverImageUrl()));
        // 변경감지로 flush
    }

    @Transactional
    public void delete(String username, Long collectionId) {
        User user = getUser(username);
        Collection collection = getOwnedCollection(user, collectionId);
        collectionRepository.delete(collection);
    }

    /* ===================== 위시템 담기/빼기 ===================== */

    /** 룩북에 위시템 담기. 룩북·위시템 모두 본인 소유여야 함. */
    @Transactional
    public void addItem(String username, Long collectionId, Long wishId) {
        User user = getUser(username);
        Collection collection = getOwnedCollection(user, collectionId);
        WishItem wish = getOwnedWish(user, wishId);
        collection.getItems().add(wish); // Set 이므로 중복 자동 무시
    }

    /** 룩북에서 위시템 빼기(위시템 자체는 삭제하지 않음). */
    @Transactional
    public void removeItem(String username, Long collectionId, Long wishId) {
        User user = getUser(username);
        Collection collection = getOwnedCollection(user, collectionId);
        collection.getItems().removeIf(w -> w.getId().equals(wishId));
    }

    /* ===================== 내부 헬퍼 ===================== */

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."));
    }

    private Collection getOwnedCollection(User user, Long collectionId) {
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "룩북을 찾을 수 없습니다."));
        if (!collection.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "룩북을 찾을 수 없습니다.");
        }
        return collection;
    }

    private WishItem getOwnedWish(User user, Long wishId) {
        WishItem wish = wishRepository.findById(wishId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "위시템을 찾을 수 없습니다."));
        if (!wish.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "위시템을 찾을 수 없습니다.");
        }
        return wish;
    }

    /** 아이템 가격 합계(null 방지). */
    private long totalPrice(Set<WishItem> items) {
        long sum = 0L;
        for (WishItem w : items) {
            if (w.getPrice() != null) {
                sum += w.getPrice();
            }
        }
        return sum;
    }

    /** 커버 이미지: 지정값 우선, 없으면 첫 아이템 썸네일, 그것도 없으면 null. */
    private String resolveCover(Collection c) {
        if (c.getCoverImageUrl() != null && !c.getCoverImageUrl().isBlank()) {
            return c.getCoverImageUrl();
        }
        for (WishItem w : c.getItems()) {
            if (w.getThumbnailUrl() != null && !w.getThumbnailUrl().isBlank()) {
                return w.getThumbnailUrl();
            }
        }
        return null;
    }

    private String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
