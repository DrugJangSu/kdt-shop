package com.kdt.shop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PostApiController.class)
@Import(ShopPageConfig.class)
class PostApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @Test
    void list_returns_200() throws Exception {
        // 준비: 실제 DB가 아닌 모의 서비스의 반환값입니다.
        given(this.postService.list(any(Pageable.class)))
                .willReturn(new PostPageResponse(
                        List.of(new PostItem(1L, "closed", "shop01"),
                                new PostItem(2L, "opened", "shop01")),
                        0, 2, 3));

        // 실행과 응답 검증: MVC가 요청을 해석하고 DTO를 JSON으로 반환합니다.
        this.mockMvc.perform(get("/api/v1/posts")
                        .param("page", "0")
                        .param("size", "2")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title").value("closed"))
                .andExpect(jsonPath("$.content[0].loginId").value("shop01"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(3));

        // 전달값 검증: 스텁의 고정 응답만으로는 확인할 수 없는 부분입니다.
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(this.postService).list(captor.capture());
        Pageable sent = captor.getValue();
        assertEquals(0, sent.getPageNumber());
        assertEquals(2, sent.getPageSize());
    }

    @Test
    void list_without_query_uses_defaults() throws Exception {
        given(this.postService.list(any(Pageable.class)))
                .willReturn(new PostPageResponse(List.of(), 0, 20, 0));

        this.mockMvc.perform(get("/api/v1/posts")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(0));

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(this.postService).list(captor.capture());
        Pageable sent = captor.getValue();
        assertEquals(0, sent.getPageNumber());
        assertEquals(20, sent.getPageSize());
    }

    @Test
    void non_numeric_id_is_400_before_service() throws Exception {
        this.mockMvc.perform(get("/api/v1/posts/abc")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(this.postService);
    }
}