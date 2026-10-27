package com.kdt.shop.querydsl;

import com.kdt.shop.Post;

/**
 * 게시글 + 작성자 정보를 JSON으로 내보내기 위한 응답 DTO.
 *
 * <p>{@code record}를 사용하면 다음이 자동으로 만들어진다.
 * <ul>
 *   <li>모든 필드를 받는 생성자</li>
 *   <li>각 필드의 접근자: id(), title(), body(), memberId(), loginId(), nickname()</li>
 *   <li>equals, hashCode, toString</li>
 * </ul>
 * Jackson은 record의 접근자를 보고 JSON 필드 이름을 정한다.
 *
 * <p>Entity(Post)를 직접 반환하지 않는 이유:
 * Entity에는 연관관계가 있어서 JSON 직렬화 시 순환 참조나 불필요한 필드 노출이 생길 수 있다.
 * DTO로 바꾸면 화면에 필요한 값만 안전하게 내보낼 수 있다.
 *
 * @param id       게시글 id
 * @param title    제목
 * @param body     본문
 * @param memberId 작성자 id
 * @param loginId  작성자 로그인 id
 * @param nickname 작성자 닉네임
 */
public record PostResponseByQueryDSL(
        Long id,
        String title,
        String body,
        Long memberId,
        String loginId,
        String nickname
) {
    /**
     * Post Entity를 응답 DTO로 변환하는 정적 팩토리 메서드.
     *
     * <p>여기서 {@code post.getMember()}를 호출한다.
     * QueryDSL 쿼리에 fetch join이 있으면 이미 Member가 메모리에 함께 로딩되어 있어서
     * 추가 SQL이 나가지 않는다. fetch join이 없으면 이 시점에 지연 로딩이 일어난다.
     * (OSIV가 꺼져 있고 트랜잭션 밖이면 LazyInitializationException이 날 수 있다.)
     */
    public static PostResponseByQueryDSL from(Post post) {
        return new PostResponseByQueryDSL(
                post.getId(),
                post.getTitle(),
                post.getBody(),
                post.getMember().getId(),
                post.getMember().getLoginId(),
                post.getMember().getNickname()
        );
    }
}
