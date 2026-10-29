package com.kdt.shop;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class PostReferenceService {
    private final PostService postService;
    private final OutsidePostClient outsidePostClient;

    @Autowired
    public PostReferenceService(PostService postService, OutsidePostClient outsidePostClient) {
        this.postService = postService;
        this.outsidePostClient = outsidePostClient;
    }

    public PostReferenceResponse one(Long id, int outsidePostId) {
        PostResponse post = this.postService.one(id);
        OutsidePost found = this.outsidePostClient.read(outsidePostId);

        if (found.body() == null) {
            throw new IllegalStateException("outside response missing");
        }

        OutsideReference reference = new OutsideReference(found.id(), found.title(), found.body());

        return new PostReferenceResponse(post, reference);
    }
    public PostReferencesResponse list(Long id, int outsideUserId) {
        PostResponse post = this.postService.one(id);
        List<OutsidePost> found = this.outsidePostClient.findByUserId(outsideUserId);
        List<OutsideReferenceItem> items = new ArrayList<>();
        for (OutsidePost outside : found) {
            if (outside == null || outside.title() == null) {
                throw new IllegalStateException("outside response missing");
            }
            items.add(new OutsideReferenceItem(outside.id(), outside.title()));
        }
        return new PostReferencesResponse(post, items);
    }

}
