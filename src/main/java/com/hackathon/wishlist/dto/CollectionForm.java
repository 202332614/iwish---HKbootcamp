package com.hackathon.wishlist.dto;

import com.hackathon.wishlist.domain.Collection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 룩북 생성/수정 공용 폼 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
public class CollectionForm {

    @NotBlank(message = "룩북 이름을 입력하세요.")
    @Size(max = 50, message = "이름은 50자 이내로 입력하세요.")
    private String name;

    @Size(max = 500, message = "설명은 500자 이내로 입력하세요.")
    private String description;

    @NotBlank(message = "스타일을 선택하세요.")
    private String styleTag;

    /** 커버 이미지 URL (선택) */
    private String coverImageUrl;

    /** 수정 화면 진입 시 기존 엔티티 값으로 폼을 채운다. */
    public static CollectionForm from(Collection c) {
        CollectionForm form = new CollectionForm();
        form.name = c.getName();
        form.description = c.getDescription();
        form.styleTag = c.getStyleTag();
        form.coverImageUrl = c.getCoverImageUrl();
        return form;
    }
}
