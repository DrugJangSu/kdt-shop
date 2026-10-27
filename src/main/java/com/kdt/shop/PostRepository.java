package com.kdt.shop;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PostRepository extends JpaRepository<Post, Long> {


    @Query(value = """
                select new com.kdt.shop.PostItem(p.id, p.title, m.loginId)
                    from Post p
                        join p.member m
            """,
            countQuery = """
                    select count(p)
                        from Post p
                            join p.member m 
                    """)
    Page<PostItem> findPageItems(Pageable pageable);

    @Query(value = """
            select p.id as id, p.title as title, m.loginId as loginId
                        from Post p
                                    join p p.member m
                        """,
            countQuery = """
                    select count(p)
                    from Post p
                    join p.member m
                    """
    )
    Page<PostListView> findPageView(Pageable pageable);
}
