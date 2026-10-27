package com.kdt.shop.querydsl;

import com.kdt.shop.Member;
import com.kdt.shop.MemberRepository;
import com.kdt.shop.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * QueryDSL 예제를 HTTP로 직접 실행해 보기 위한 "테스트용" 컨트롤러.
 *
 * <p>학습 편의를 위해 Service 없이 컨트롤러가 리포지토리를 바로 호출한다.
 * 실제 프로젝트에서는 Service에 @Transactional을 붙이고
 * Controller → Service → Repository 순서로 나누는 경우가 많다.
 *
 * <p>기본 주소: {@code /api/v1/querydsl}
 * <ul>
 *   <li>POST /members        : 테스트용 회원 생성</li>
 *   <li>POST /posts          : 테스트용 게시글 생성</li>
 *   <li>GET  /posts/{id}     : 단건 조회(QueryDSL + fetch join)</li>
 *   <li>GET  /posts          : 목록 조회(QueryDSL + fetch join)</li>
 *   <li>GET  /posts/page     : 페이징 조회(QueryDSL + fetch join)</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/querydsl")
@RequiredArgsConstructor
public class PostQuerydslTestController {

    /**
     * 기본 CRUD(JpaRepository)와 QueryDSL 메서드를 모두 가진 리포지토리.
     * save()는 JpaRepository, findPostWithMember()는 PostRepositoryCustom에서 온다.
     */
    private final PostTempRepository postRepository;

    /** 회원을 만들고 찾기 위한 기본 리포지토리. */
    private final MemberRepository memberRepository;

    // =====================================================================
    // 1. 테스트용 회원 생성
    //    POST /api/v1/querydsl/members
    //    요청: {"loginId":"shop02","nickname":"querydsl"}
    //    응답: 생성된 회원 id (숫자 하나)
    // =====================================================================
    @PostMapping("/members")
    public ResponseEntity<Long> createMember(@RequestBody MemberCreateRequest request) {
        // Jackson이 JSON 본문을 MemberCreateRequest record로 변환해서 넘겨 준다.
        Member member = new Member();
        member.setLoginId(request.loginId());
        member.setNickname(request.nickname());

        // save()는 QueryDSL이 아니라 JpaRepository가 제공하는 기본 메서드다.
        Member saved = memberRepository.save(member);
        return ResponseEntity.ok(saved.getId());
    }

    // =====================================================================
    // 2. 테스트용 게시글 생성
    //    POST /api/v1/querydsl/posts
    //    요청: {"memberId":1,"title":"제목","body":"내용"}
    //    응답: 생성된 게시글 id (숫자 하나)
    // =====================================================================
    @PostMapping("/posts")
    public ResponseEntity<Long> createPost(@RequestBody PostCreateRequest request) {
        // 게시글은 반드시 작성자(Member)가 있어야 하므로 먼저 회원을 찾는다.
        // 없으면 IllegalArgumentException이 발생한다.
        // 이 예제에는 별도 핸들러가 없어서 클라이언트는 500 응답을 받는다.
        Member member = memberRepository.findById(request.memberId())
                                        .orElseThrow(() -> new IllegalArgumentException("회원이 없습니다. id=" + request.memberId()));

        Post post = new Post();
        post.setMember(member);          // N:1 연관관계 설정
        post.setTitle(request.title());
        post.setBody(request.body());

        Post saved = postRepository.save(post);
        return ResponseEntity.ok(saved.getId());
    }

    // =====================================================================
    // 3. 단건 조회 (QueryDSL + fetch join)
    //    GET /api/v1/querydsl/posts/{id}
    // =====================================================================
    @GetMapping("/posts/{id}")
    public ResponseEntity<PostResponseByQueryDSL> getPost(@PathVariable Long id) {
        // PostTempRepositoryImpl.findPostWithMember()가 실행된다.
        // 게시글과 작성자를 SQL 한 번으로 가져오므로, 아래 from()에서
        // post.getMember()를 호출해도 추가 쿼리가 나가지 않는다.
        Post post = postRepository.findPostWithMember(id);

        if (post == null) {
            return ResponseEntity.notFound().build();   // 404
        }

        return ResponseEntity.ok(PostResponseByQueryDSL.from(post));
    }

    // =====================================================================
    // 4. 목록 조회 (QueryDSL + fetch join)
    //    GET /api/v1/querydsl/posts
    // =====================================================================
    @GetMapping("/posts")
    public ResponseEntity<List<PostResponseByQueryDSL>> getAllPosts() {
        // Entity(Post)를 그대로 반환하지 않고 DTO로 변환한다.
        // - Entity를 그대로 JSON으로 만들면 연관관계 직렬화 문제가 생길 수 있다.
        // - 화면에 필요한 필드만 골라서 내보낼 수 있다.
        List<PostResponseByQueryDSL> responses = postRepository.findAllWithMember().stream()
                                                               .map(PostResponseByQueryDSL::from)
                                                               .toList();
        return ResponseEntity.ok(responses);
    }

    // =====================================================================
    // 5. 페이징 조회 (QueryDSL + fetch join)
    //    GET /api/v1/querydsl/posts/page?page=0&size=3
    //
    //    참고: /posts/page 는 /posts/{id} 보다 구체적인 경로라서
    //          {id}에 "page"가 들어가는 대신 이 메서드가 선택된다.
    // =====================================================================
    @GetMapping("/posts/page")
    public ResponseEntity<Page<PostResponseByQueryDSL>> getPostsPage(Pageable pageable) {
        // 스프링 MVC가 ?page=0&size=3 같은 쿼리 파라미터를 Pageable로 변환해 준다.
        // Page.map()은 페이지 내용물만 DTO로 바꾸고, 페이지 정보(totalElements 등)는 유지한다.
        Page<PostResponseByQueryDSL> responses = postRepository.findPageWithMember(pageable)
                                                               .map(PostResponseByQueryDSL::from);
        return ResponseEntity.ok(responses);
    }
}
