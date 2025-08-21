package cc.nilm.blog.repository;

import cc.nilm.blog.entity.Post;
import cc.nilm.blog.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByPublishedTrue(Pageable pageable);
    Page<Post> findByAuthor(User author, Pageable pageable);
    Page<Post> findByTitleContainingOrContentContaining(String title, String content, Pageable pageable);
    Page<Post> findByCategoryId(Long categoryId, Pageable pageable);
    Page<Post> findByTagsId(Long tagId, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.id = :id")
    Optional<Post> findByIdWithComments(Long id);
}