package com.kdt.shop;

import org.hibernate.annotations.NotFound;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
    @Cacheable(cacheNames = "shopPosts", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public PostPageResponse list(Pageable pageable) {
        PageRequest fixed = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by("title").and(Sort.by("id"))
        );

//        Page<Post> found = this.postRepository.findAll(fixed);
        Page<PostItem> found = this.postRepository.findPageItems(fixed);
//        List<PostItem> items = new ArrayList<>();
        List<PostItem> items = new ArrayList<>(found.getContent());

//        for (Post post : found.getContent()) {
//            items.add(new PostItem(post.getId(), post.getTitle(), post.getMember().getLoginId()));
//        }

        return new PostPageResponse(items, found.getNumber(), found.getSize(), found.getTotalElements());
    }
    @Transactional(readOnly = true)
    public PostResponse one(Long id) {

        Post post = this.postRepository.findById(id).orElseThrow(() -> new
                NotFoundException("post missing"));

        return new PostResponse(post.getId(), post.getTitle(), post.getMember()
                .getLoginId());
    }
    @Transactional
    @CacheEvict(cacheNames = "shopPosts", allEntries = true)
    public PostResponse create(PostCreateRequest request) {
        Member member = this.memberRepository.findByLoginId("shop01").orElseThrow();

        Post post = new Post();
        post.setMember(member);
        post.setTitle(request.title());
        post.setBody(request.body());

        Post saved = this.postRepository.save(post);

        return new PostResponse(saved.getId(), saved.getTitle(), saved.getMember().getLoginId());
    }
    @Transactional
    @CacheEvict(cacheNames = "shopPosts", allEntries = true)
    public void delete(Long id) {
        Post post = this.postRepository.findById(id).orElseThrow(() -> new NotFoundException("post missing"));
        this.postRepository.delete(post);
    }
}