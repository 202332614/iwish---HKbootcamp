package com.hackathon.wishlist.domain;

/**
 * 사용자 권한.
 * Spring Security에는 "ROLE_" 접두사를 붙여 ROLE_USER / ROLE_ADMIN 형태로 부여한다.
 */
public enum Role {
    USER,
    ADMIN
}
