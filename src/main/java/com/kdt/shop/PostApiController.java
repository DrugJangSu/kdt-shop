package com.kdt.shop;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "posts", description = "게시글 조회·생성·삭제 API")
@RestController
@RequestMapping("/api/v1/posts")
public class PostApiController {
    private final PostService postService;

    @Autowired
    public PostApiController(PostService postService) {
        this.postService = postService;
    }

    @Operation(summary = "게시글 목록 조회",
            description = "page는 0부터, 기본 size는 20입니다. 정렬은 title·id 오름차순으로 고정합니다.")
    @ApiResponse(responseCode = "200", description = "목록 조회 성공 (글이 없는 페이지도 200, content는 빈 배열)")
    @GetMapping
    public PostPageResponse list(@ParameterObject Pageable pageable) {
        return this.postService.list(pageable);
    }

    @Operation(summary = "게시글 단건 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 번호의 글이 없음",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/{id}")
    public PostResponse one(@PathVariable("id") Long id) {
        return this.postService.one(id);
    }

    @Operation(summary = "게시글 생성")
    @ApiResponse(responseCode = "201", description = "생성 성공")
    @PostMapping
    public ResponseEntity<PostResponse> create(@RequestBody PostCreateRequest request) {
        PostResponse saved = this.postService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @Operation(summary = "게시글 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공 (본문 없음)"),
            @ApiResponse(responseCode = "404", description = "해당 번호의 글이 없음",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) {
        this.postService.delete(id);
    }
}