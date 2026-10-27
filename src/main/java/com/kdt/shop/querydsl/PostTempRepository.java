package com.kdt.shop.querydsl;

import com.kdt.shop.Post;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 실제로 스프링 빈으로 등록되는 리포지토리 인터페이스.
 *
 * <p>두 부모를 함께 상속한다.
 * <ul>
 *   <li>{@link JpaRepository}: save, findById, delete, findAll 등 기본 CRUD 기능</li>
 *   <li>{@link PostRepositoryCustom}: QueryDSL로 직접 만든 조회 메서드</li>
 * </ul>
 *
 * <p>구현체는 같은 패키지의 {@link PostTempRepositoryImpl}이다.
 * Spring Data JPA가 이름 규칙으로 구현체를 찾아서,
 * 기본 CRUD와 QueryDSL 메서드를 하나의 프록시 객체로 합쳐 준다.
 *
 * <p>컨트롤러에서는 이 인터페이스만 주입받아 사용하면 된다.
 * (JpaRepository의 메서드와 QueryDSL 메서드를 모두 호출할 수 있다.)
 */
public interface PostTempRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {
}
