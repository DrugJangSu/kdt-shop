package com.kdt.shop.querydsl;

import com.kdt.shop.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * QueryDSL로 직접 구현할 메서드의 "목차" 역할을 하는 인터페이스.
 *
 * <p>여기에는 메서드 선언만 있고 구현은 없다.
 * 실제 구현은 같은 패키지의 {@link PostTempRepositoryImpl}에 작성한다.
 *
 * <p>Spring Data JPA는 {@link PostTempRepository}를 만들 때
 * 이 인터페이스의 구현체를 찾아서 기본 CRUD 기능과 합쳐 준다.
 * (이렇게 하면 기본 기능은 JpaRepository에서, 복잡한 조회는 QueryDSL에서 가져올 수 있다.)
 */
public interface PostRepositoryCustom {

    /**
     * 게시글 1건을 작성자(Member)까지 함께 조회한다.
     *
     * @param id 게시글 id
     * @return 게시글. 없으면 null
     */
    Post findPostWithMember(Long id);

    /**
     * 모든 게시글을 작성자(Member)까지 함께 조회한다.
     *
     * @return 게시글 목록. 없으면 빈 리스트
     */
    List<Post> findAllWithMember();

    /**
     * 게시글을 페이지 단위로 조회하면서 작성자(Member)까지 함께 가져온다.
     *
     * @param pageable 페이지 번호, 페이지 크기, 정렬 정보
     * @return 페이지 결과(내용 + 전체 개수 + 페이지 정보)
     */
    Page<Post> findPageWithMember(Pageable pageable);
}
