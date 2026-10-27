package com.kdt.shop.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * QueryDSL을 사용하기 위한 설정 클래스.
 *
 * <p>QueryDSL 쿼리는 {@link JPAQueryFactory}에서 시작한다.
 * 이 빈을 한 번 만들어 두면, 리포지토리 구현체에서 생성자로 주입받아 계속 재사용할 수 있다.
 */
@Configuration
public class QueryDslConfig {

    /**
     * {@code @PersistenceContext}는 스프링이 관리하는 EntityManager "프록시"를 주입한다.
     *
     * <p>진짜 EntityManager 하나를 모든 요청이 같이 쓰는 것이 아니라,
     * 현재 트랜잭션(또는 요청)에 연결된 EntityManager로 연결해 주는 대리 객체다.
     * 그래서 여러 스레드가 동시에 사용해도 안전하다.
     */
    @PersistenceContext
    private EntityManager entityManager;

    /**
     * QueryDSL 쿼리를 만드는 공장(Factory).
     *
     * <p>JPAQueryFactory는 쿼리를 실행할 때마다 새로운 JPAQuery 객체를 만들기 때문에
     * 상태를 가지지 않는다. 따라서 싱글톤 빈으로 공유해도 된다.
     *
     * @return EntityManager에 연결된 JPAQueryFactory
     */
    @Bean
    public JPAQueryFactory queryFactory() {
        return new JPAQueryFactory(entityManager);
    }
}
