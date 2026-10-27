package com.kdt.shop.querydsl;

/**
 * 회원 생성 요청 JSON을 받는 DTO.
 *
 * <p>예: {@code {"loginId":"shop02","nickname":"querydsl"}}
 * loginId는 DB에서 unique이므로 이미 있는 값이면 저장에 실패한다.
 *
 * @param loginId  로그인 id
 * @param nickname 닉네임
 */
public record MemberCreateRequest(
        String loginId,
        String nickname
) {}
