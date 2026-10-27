package com.kdt.shop.querydsl;

import com.kdt.shop.Post;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

// QPost, QMember의 static 필드를 그대로 가져와서 post, member 라는 이름으로 사용한다.
// 예: QPost.post -> post, QMember.member -> member
import static com.kdt.shop.QMember.member;
import static com.kdt.shop.QPost.post;

/**
 * QueryDSL 조회 메서드의 실제 구현체.
 *
 * <p><b>클래스 이름이 중요한 이유</b><br>
 * Spring Data JPA는 리포지토리 인터페이스({@link PostTempRepository})를 만들 때
 * 다음 두 이름 규칙 중 하나로 구현체를 찾는다.
 * <ul>
 *   <li>리포지토리 인터페이스 이름 + Impl → {@code PostTempRepositoryImpl} (현재 사용)</li>
 *   <li>커스텀 인터페이스 이름 + Impl → {@code PostRepositoryCustomImpl}</li>
 * </ul>
 * 이름이 이 규칙과 다르면 스프링이 구현체를 찾지 못해
 * QueryDSL 메서드를 호출할 수 없다. (가장 흔한 실수!)
 *
 * <p>{@code @RequiredArgsConstructor}는 final 필드를 받는 생성자를 자동으로 만들어 준다.
 * 덕분에 JPAQueryFactory가 스프링에 의해 생성자 주입된다.
 */
@RequiredArgsConstructor
public class PostTempRepositoryImpl implements PostRepositoryCustom {

    /**
     * QueryDSL 쿼리의 시작점.
     * {@link QueryDslConfig}에서 만든 빈이 생성자로 주입된다.
     */
    private final JPAQueryFactory queryFactory;

    // =====================================================================
    // ① 단건 조회 + 패치 조인
    // =====================================================================
    @Override
    public Post findPostWithMember(Long id) {
        return queryFactory
                // select(post).from(post)를 한 줄로 줄인 축약형.
                // QPost의 static 인스턴스(post)를 기준으로 shop_post 테이블을 조회한다.
                .selectFrom(post)

                // post.member 연관관계를 member라는 별칭으로 조인한다.
                // fetchJoin()이 핵심이다:
                //   - 일반 join()은 "조건에 맞는 게시글"만 가져오고 Member는 가져오지 않는다.
                //   - fetchJoin()은 게시글과 작성자를 SQL 한 번으로 함께 가져온다.
                //     → 나중에 post.getMember()를 호출해도 추가 쿼리가 나가지 않는다. (N+1 방지)
                .join(post.member, member).fetchJoin()

                // post.id == 전달받은 id 인 행만 남긴다.
                .where(post.id.eq(id))

                // 결과 1건을 Post 객체로 반환한다.
                // - 결과가 없으면 null
                // - 결과가 2건 이상이면 NonUniqueResultException
                .fetchOne();
    }

    // =====================================================================
    // ② 목록 조회 + 패치 조인
    // =====================================================================
    @Override
    public List<Post> findAllWithMember() {
        return queryFactory
                .selectFrom(post)
                .join(post.member, member).fetchJoin()

                // fetch(): 결과를 List로 모두 가져온다.
                // 결과가 없으면 null이 아니라 빈 리스트(empty list)를 돌려준다.
                .fetch();
    }

    // =====================================================================
    // ③ 페이징 조회 + 패치 조인
    // =====================================================================
    @Override
    public Page<Post> findPageWithMember(Pageable pageable) {

        // (1) 현재 페이지에 보여 줄 데이터만 조회한다.
        List<Post> content = queryFactory
                .selectFrom(post)
                .join(post.member, member).fetchJoin()

                // Pageable이 계산해 준 시작 위치와 개수를 적용한다.
                // 예: page=1, size=3 → offset=3, limit=3
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())

                // 페이징에는 정렬이 필수다.
                // 정렬이 없으면 DB가 매번 다른 순서로 행을 돌려줄 수 있어서
                // 페이지를 넘길 때 같은 글이 중복되거나 빠질 수 있다.
                //
                // 참고: 지금은 학습용으로 id 내림차순을 고정했다.
                // 요청의 sort 파라미터까지 반영하려면 pageable.getSort()를
                // OrderSpecifier로 변환해서 넘겨야 한다.
                .orderBy(post.id.desc())
                .fetch();

        // (2) 전체 개수를 세는 카운트 쿼리.
        //     fetchJoin은 개수를 세는 데 필요 없고, 오히려 결과를 부풀릴 수 있으므로 넣지 않는다.
        //     지금은 모든 Post가 Member를 반드시 가지므로(member_id NOT NULL, inner join)
        //     조인 없이 post만 세도 content 쿼리와 같은 개수가 나온다.
        //
        //     ⚠️ 만약 content 쿼리에 where 조건이나 left join이 있다면
        //        count 쿼리에도 같은 조건을 넣어야 총 개수가 맞는다.
        JPAQuery<Long> countQuery = queryFactory
                .select(post.count())
                .from(post);

        // (3) content와 전체 개수로 Page 객체를 만든다.
        //     PageableExecutionUtils는 다음과 같은 경우 count 쿼리를 생략하는 최적화를 해 준다.
        //       - 첫 페이지인데 content가 pageSize보다 적으면 → 전체 개수 = content.size()
        //       - 마지막 페이지임을 알 수 있으면 → 전체 개수 = offset + content.size()
        //     countQuery::fetchOne 은 "필요할 때만 호출되는" 지연 공급자(LongSupplier)다.
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }
}
