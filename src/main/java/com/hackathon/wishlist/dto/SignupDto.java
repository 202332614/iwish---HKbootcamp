package com.hackathon.wishlist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 회원가입 폼 바인딩 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
public class SignupDto {

    @NotBlank(message = "아이디를 입력하세요.")
    @Size(min = 3, max = 20, message = "아이디는 3~20자로 입력하세요.")
    private String username;

    @NotBlank(message = "비밀번호를 입력하세요.")
    @Size(min = 4, max = 64, message = "비밀번호는 4자 이상 입력하세요.")
    private String password;
}
