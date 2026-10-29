package com.kdt.shop;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/posts")
public class PostReferenceController {

    private final PostReferenceService postReferenceService;

    @Autowired
    public PostReferenceController(PostReferenceService postReferenceService) {
        this.postReferenceService = postReferenceService;
    }

    @GetMapping("/{id}/reference")
    public PostReferenceResponse one(@PathVariable("id") Long id, @RequestParam("outsidePostId") int outsidePostId) {

        return this.postReferenceService.one(id, outsidePostId);
    }

    @GetMapping("/{id}/references")
    public PostReferencesResponse list(@PathVariable("id") Long id, @RequestParam("outsideUserId") int outsideUserId) {

        return this.postReferenceService.list(id, outsideUserId);

    }

}