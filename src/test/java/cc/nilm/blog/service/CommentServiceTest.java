package cc.nilm.blog.service;

import cc.nilm.blog.entity.Comment;
import cc.nilm.blog.entity.Post;
import cc.nilm.blog.entity.User;
import cc.nilm.blog.repository.CommentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentService commentService;

    private Comment comment1, comment2;
    private Post post;
    private User user;
    private Pageable pageable;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        post = new Post();
        post.setId(1L);
        post.setTitle("Test Post");

        comment1 = new Comment();
        comment1.setId(1L);
        comment1.setContent("First comment");
        comment1.setAuthor(user);
        comment1.setPost(post);
        comment1.setCreatedAt(LocalDateTime.now());

        comment2 = new Comment();
        comment2.setId(2L);
        comment2.setContent("Second comment");
        comment2.setAuthor(user);
        comment2.setPost(post);
        comment2.setCreatedAt(LocalDateTime.now().minusHours(1));

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void findAllByPostId_ShouldReturnCommentsForPost() {
        // given
        Page<Comment> commentPage = new PageImpl<>(Arrays.asList(comment1, comment2));
        when(commentRepository.findAllByPostId(post.getId(), pageable)).thenReturn(commentPage);

        // when
        Page<Comment> result = commentService.findAllByPostId(post.getId(), pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        verify(commentRepository).findAllByPostId(post.getId(), pageable);
    }

    @Test
    void findByAuthorId_ShouldReturnCommentsByUser() {
        // given
        Page<Comment> commentPage = new PageImpl<>(Arrays.asList(comment1, comment2));
        when(commentRepository.findAllByAuthorId(user.getId(), pageable)).thenReturn(commentPage);
        // when
        Page<Comment> result = commentService.findAllByUserId(user.getId(), pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        verify(commentRepository).findAllByAuthorId(user.getId(), pageable);
    }

    @Test
    void findById_ShouldReturnComment() {
        // given
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment1));

        // when
        Optional<Comment> result = commentService.findById(1L);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getContent()).isEqualTo("First comment");
        verify(commentRepository).findById(1L);
    }

    @Test
    void save_ShouldPersistComment() {
        // given
        when(commentRepository.save(any(Comment.class))).thenReturn(comment1);

        // when
        Comment result = commentService.save(comment1);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo("First comment");
        verify(commentRepository).save(comment1);
    }

}
