package com.kdt.shop;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;

import java.time.Duration;

/**
 * Redis 캐시 설정 클래스
 *
 * [왜 이 설정이 필요한가요?]
 * Redis는 자바 객체(Object)를 그대로 저장할 수 없고, 오직 "문자열(JSON)" 형태로만 저장합니다.
 * 그래서 객체를 JSON으로 바꿔 저장(직렬화)하고, 꺼낼 때 다시 객체로 복원(역직렬화)합니다.
 *
 * [문제 상황]
 * 예전 코드는 JSON에 "@class"(원래 클래스 이름) 정보가 없이 저장했습니다.
 * 그래서 캐시에서 꺼낼 때 Jackson이 "이게 원래 어떤 클래스였는지" 몰라서
 * LinkedHashMap 이라는 범용 Map으로 반환했고, PostService에서
 * (PostPageResponse) 로 캐스팅하다가 ClassCastException이 터졌습니다.
 *
 * [해결 방법]
 * 1) 아래 설정에서 enableDefaultTyping(typeValidator)를 호출해
 *    "일반 클래스"에는 JSON에 "@class"가 붙도록 합니다.
 * 2) record는 자바에서 자동으로 final 클래스가 되는데,
 *    Jackson 3에서는 final 클래스에 "@class"를 자동으로 붙여주지 않습니다.
 *    그래서 record(PostPageResponse, PostItem)에는
 *    @JsonTypeInfo 어노테이션을 직접 붙여 타입 정보를 심습니다.
 *    (이 부분은 PostPageResponse.java, PostItem.java 쪽에서 처리)
 *
 * 이렇게 두 가지를 함께 적용하면 record도 캐시에서 정상적으로 복원됩니다.
 */
@Configuration
@EnableCaching // @Cacheable, @CacheEvict 같은 캐시 어노테이션을 활성화합니다.
public class ShopCacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {

        /*
         * [1] 다형성 타입 검증기 (PolymorphicTypeValidator) 만들기
         *
         * - 역직렬화 시 JSON에 적힌 "@class" 값을 읽고, 그 클래스로 객체를 복원합니다.
         * - 이 검증기는 "어떤 클래스까지 복원을 허용할 것인가?"를 결정합니다.
         * - allowIfSubType(Object.class) => Object의 하위 타입이면 모두 허용.
         *   (내부 캐시라서 신뢰할 수 있는 데이터만 들어오기 때문에 이렇게 설정합니다.)
         * - 보안을 더 강화하고 싶다면 아래처럼 특정 패키지만 허용할 수도 있습니다.
         *   .allowIfSubType("com.kdt.shop.")
         */
        PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType(Object.class)
                .build();

        /*
         * [2] Redis 저장값(Value) 직렬화기 만들기
         *
         * - GenericJacksonJsonRedisSerializer: 자바 객체 ↔ JSON 변환을 담당합니다.
         * - enableDefaultTyping(typeValidator):
         *     - JSON에 "@class" 필드를 함께 저장하도록 합니다.
         *     - 기본 동작은 NON_FINAL과 유사하게 동작합니다.
         *       즉, 일반 클래스에는 "@class"가 자동으로 붙지만
         *       record처럼 final로 선언된 클래스에는 붙지 않습니다.
         *     - 그래서 record 쪽에는 @JsonTypeInfo 어노테이션으로
         *       타입 정보를 직접 심어줄 예정입니다.
         *     - 참고: Jackson 3에서는 DefaultTyping.EVERYTHING 옵션이
         *       제거되었습니다. (보안과 JSON 크기 문제 때문)
         * - enableSpringCacheNullValueSupport():
         *     - 캐시에 null 값을 저장할 수 있게 해줍니다.
         *     - 예: 메서드가 null을 반환할 때 캐시에 "null"로 저장해두고
         *       다음 호출 때 DB를 다시 조회하지 않게 됩니다. (선택 사항이지만 실무에서 유용)
         */
        GenericJacksonJsonRedisSerializer jsonSerializer = GenericJacksonJsonRedisSerializer.builder()
                .enableDefaultTyping(typeValidator)
                .enableSpringCacheNullValueSupport()
                .build();

        /*
         * [3] 캐시 설정 구성
         *
         * - entryTtl(Duration.ofMinutes(10)): 10분 뒤에 자동 삭제 (무한정 쌓이는 것 방지)
         * - serializeKeysWith(StringRedisSerializer):
         *     - Redis의 Key는 사람이 읽을 수 있는 "문자열"로 저장합니다.
         *     - 예: "shopPosts::0-2" 형태로 깔끔하게 저장됩니다.
         * - serializeValuesWith(jsonSerializer):
         *     - Redis의 Value는 위에서 만든 JSON 직렬화기로 저장합니다.
         *     - Redis에서 직접 조회해도 사람이 읽을 수 있는 JSON을 볼 수 있습니다.
         */
        RedisCacheConfiguration shopPostsConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer)
                );

        /*
         * [4] 캐시 매니저 생성
         *
         * - cacheDefaults(shopPostsConfig): 위에서 만든 설정을 기본값으로 사용합니다.
         * - 그래서 @Cacheable("shopPosts") 어노테이션만 붙이면 자동으로 이 설정이 적용됩니다.
         * - 결과 키 예시: shopPosts::0-2
         */
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(shopPostsConfig)
                .build();
    }
}