package cc.nilm.blog.service;

import cc.nilm.blog.entity.Comment;
import cc.nilm.blog.repository.CommentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;

    public Page<Comment> findAllByPostId(Long postId, Pageable pageable) {
        return commentRepository.findAllByPostId(postId, pageable);
    }

    public Optional<Comment> findById(Long id) {
        return commentRepository.findById(id);
    }

    public Page<Comment> findAllByUserId(Long userId, Pageable pageable) {
        return commentRepository.findAllByAuthorId(userId, pageable);
    }

    @Transactional
    public Comment save(Comment comment) {
        return commentRepository.save(comment);
    }

    @Transactional
    public void delete(Long id) {
        commentRepository.deleteById(id);
    }
}