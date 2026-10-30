package com.kdt.shop;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

// 컨트롤러와 MVC 설정만 테스트한다. 실제 DB·Redis나 8080 서버는 사용하지 않는다.
@WebMvcTest(PostApiController.class)
// 페이지 처리·CORS 설정과 기존 404 예외 처리를 테스트에 포함한다.
@Import({ShopPageConfig.class, ShopCorsConfig.class, ApiExceptionAdvice.class})
class ShopCorsConfigTest {
    private static final String ORIGIN = "http://127.0.0.1:8000";

    @Autowired
    private MockMvc mockMvc;

    // 실제 PostService 대신 테스트에서 반환값을 지정하는 mock 객체를 사용한다.
    @MockitoBean
    private PostService postService;

    @Test
        // 허용 출처의 GET이 200과 Allow-Origin 헤더를 받고 서비스를 호출하는지 확인한다.
    void allowed_origin_can_read_list() throws Exception {
        // 실제 DB를 조회하지 않고 빈 목록 응답을 반환하도록 준비한다.
        given(this.postService.list(any(Pageable.class)))
                .willReturn(new PostPageResponse(List.of(), 0, 2, 0));

        this.mockMvc.perform(get("/api/v1/posts")
                        .param("page", "0").param("size", "2")
                        .header("Origin", ORIGIN)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", ORIGIN))
                .andExpect(jsonPath("$.totalElements").value(0));
        verify(this.postService).list(any(Pageable.class));
    }

    @Test
        // OPTIONS는 저장 요청이 아니다. POST와 Content-Type 허용 여부만 확인한다.
    void post_preflight_does_not_call_service() throws Exception {
        this.mockMvc.perform(options("/api/v1/posts")
                        .header("Origin", ORIGIN)
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", ORIGIN))
                .andExpect(header().string("Access-Control-Allow-Methods", containsString("POST")))
                .andExpect(header().string("Access-Control-Allow-Headers", equalToIgnoringCase("content-type")));
        // 요청이 MVC 처리 단계에서 끝나 실제 서비스 메서드를 호출하지 않았는지 확인한다.
        verifyNoInteractions(this.postService);
    }

    @Test
        // localhost는 127.0.0.1과 다른 출처이므로 서비스 호출 전에 거절되어야 한다.
    void unlisted_origin_is_rejected_before_service() throws Exception {
        this.mockMvc.perform(options("/api/v1/posts")
                        .header("Origin", "http://localhost:8000")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
        // 요청이 MVC 처리 단계에서 끝나 실제 서비스 메서드를 호출하지 않았는지 확인한다.
        verifyNoInteractions(this.postService);
    }

    @Test
        // 콤마가 빠진 JSON은 400이어야 하며 오류 응답에도 허용 출처 헤더가 있어야 한다.
    void unreadable_json_keeps_cors_header() throws Exception {
        this.mockMvc.perform(post("/api/v1/posts")
                        .header("Origin", ORIGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"draft\" \"body\":\"menu\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Access-Control-Allow-Origin", ORIGIN));
        // 요청이 MVC 처리 단계에서 끝나 실제 서비스 메서드를 호출하지 않았는지 확인한다.
        verifyNoInteractions(this.postService);
    }
}