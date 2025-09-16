package cc.nilm.blog.service;

import cc.nilm.blog.entity.Post;
import cc.nilm.blog.entity.User;
import cc.nilm.blog.repository.PostRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    public Page<Post> findPublishedPosts(Pageable pageable) {
        return postRepository.findByPublishedTrue(pageable);
    }

    public Page<Post> findAllPosts(Pageable pageable) {
        return postRepository.findAll(pageable);
    }

    public Page<Post> findByAuthor(User author, Pageable pageable) {
        return postRepository.findByAuthor(author, pageable);
    }

    public Page<Post> searchPosts(String query, Pageable pageable) {
        return postRepository.findByTitleContainingOrContentContaining(query, query, pageable);
    }

    public Page<Post> findByCategoryId(Long categoryId, Pageable pageable) {
        return postRepository.findByCategoryId(categoryId, pageable);
    }

    public Page<Post> findByTagId(Long tagId, Pageable pageable) {
        return postRepository.findByTagsId(tagId, pageable);
    }

    public Optional<Post> findById(Long id) {
        return postRepository.findById(id);
    }

    @Transactional
    public Optional<Post> findByIdWithComments(Long id) {
        return postRepository.findByIdWithComments(id);
    }

    @Transactional
    public Post save(Post post) {
        return postRepository.save(post);
    }

    @Transactional
    public void delete(Long id) {
        postRepository.deleteById(id);
    }
}