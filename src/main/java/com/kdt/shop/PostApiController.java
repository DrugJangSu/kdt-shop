package com.kdt.shop;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/posts")
public class PostApiController {
    private final PostService postService;

    @Autowired
    public PostApiController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public PostPageResponse list(Pageable pageable) {
        return this.postService.list(pageable);
    }

    @GetMapping("/{id}")
    public PostResponse one(@PathVariable("id") Long id) {
        return this.postService.one(id);
    }

    @PostMapping
    public ResponseEntity<PostResponse> create(@RequestBody PostCreateRequest request) {
        PostResponse saved = this.postService.create(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);

    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) {
        this.postService.delete(id);


    }
}
