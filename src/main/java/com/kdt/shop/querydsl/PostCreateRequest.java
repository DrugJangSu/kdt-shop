package com.kdt.shop.querydsl;

/**
 * 게시글 생성 요청 JSON을 받는 DTO.
 *
 * <p>예: {@code {"memberId":1,"title":"제목","body":"내용"}}
 * Jackson이 JSON의 각 키를 record의 필드에 맞춰 변환한다.
 *
 * @param memberId 작성자 회원 id
 * @param title    제목
 * @param body     본문
 */
public record PostCreateRequest(
        Long memberId,
        String title,
        String body
) {}
