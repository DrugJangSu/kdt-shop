package com.kdt.shop;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class PostService {

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;


    @Autowired
    public PostService(MemberRepository memberRepository, PostRepository postRepository) {
        this.memberRepository = memberRepository;
        this.postRepository = postRepository;
    }

    @Transactional(readOnly = true)
    public PostPageResponse list(Pageable pageable) {
        PageRequest fixed = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by("title").and(Sort.by("id"))
        );

        Page<Post> found = this.postRepository.findAll(fixed);
        List<PostItem> items = new ArrayList<>();

        for (Post post : found.getContent()) {
            items.add(new PostItem(post.getId(), post.getTitle(), post.getMember().getLoginId()));
        }

        return new PostPageResponse(items, found.getNumber(), found.getSize(), found.getTotalElements());


    }


}