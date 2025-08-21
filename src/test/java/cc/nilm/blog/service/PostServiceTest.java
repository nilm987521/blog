package cc.nilm.blog.service;

import cc.nilm.blog.entity.Category;
import cc.nilm.blog.entity.Post;
import cc.nilm.blog.entity.Tag;
import cc.nilm.blog.entity.User;
import cc.nilm.blog.repository.PostRepository;
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
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostService postService;

    private User author;
    private Post post1, post2;
    private Pageable pageable;

    @BeforeEach
    void setup() {
        author = new User();
        author.setId(1L);
        author.setUsername("testuser");

        Category category = new Category();
        category.setId(1L);
        category.setName("Technology");

        Tag tag = new Tag();
        tag.setId(1L);
        tag.setName("Java");
        Set<Tag> tags = new HashSet<>();
        tags.add(tag);

        post1 = new Post();
        post1.setId(1L);
        post1.setTitle("First Post");
        post1.setContent("Content of first post");
        post1.setPublished(true);
        post1.setAuthor(author);
        post1.setCategory(category);
        post1.setTags(tags);
        post1.setCreatedAt(LocalDateTime.now());

        post2 = new Post();
        post2.setId(2L);
        post2.setTitle("Second Post");
        post2.setContent("Content of second post");
        post2.setPublished(false);
        post2.setAuthor(author);
        post2.setCreatedAt(LocalDateTime.now().minusDays(1));

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void findPublishedPosts_ShouldReturnPublishedPosts() {
        // given
        Page<Post> postPage = new PageImpl<>(Collections.singletonList(post1));
        when(postRepository.findByPublishedTrue(pageable)).thenReturn(postPage);

        // when
        Page<Post> result = postService.findPublishedPosts(pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("First Post");
        verify(postRepository).findByPublishedTrue(pageable);
    }

    @Test
    void findByAuthor_ShouldReturnAuthorPosts() {
        // given
        Page<Post> postPage = new PageImpl<>(Arrays.asList(post1, post2));
        when(postRepository.findByAuthor(author, pageable)).thenReturn(postPage);

        // when
        Page<Post> result = postService.findByAuthor(author, pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        verify(postRepository).findByAuthor(author, pageable);
    }

    @Test
    void searchPosts_ShouldReturnMatchingPosts() {
        // given
        String query = "Post";
        Page<Post> postPage = new PageImpl<>(Arrays.asList(post1, post2));
        when(postRepository.findByTitleContainingOrContentContaining(query, query, pageable)).thenReturn(postPage);

        // when
        Page<Post> result = postService.searchPosts(query, pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        verify(postRepository).findByTitleContainingOrContentContaining(query, query, pageable);
    }

    @Test
    void findByCategoryId_ShouldReturnCategoryPosts() {
        // given
        Long categoryId = 1L;
        Page<Post> postPage = new PageImpl<>(Collections.singletonList(post1));
        when(postRepository.findByCategoryId(categoryId, pageable)).thenReturn(postPage);

        // when
        Page<Post> result = postService.findByCategoryId(categoryId, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCategory().getId()).isEqualTo(categoryId);
        verify(postRepository).findByCategoryId(categoryId, pageable);
    }

    @Test
    void findByTagId_ShouldReturnTaggedPosts() {
        // given
        Long tagId = 1L;
        Page<Post> postPage = new PageImpl<>(Collections.singletonList(post1));
        when(postRepository.findByTagsId(tagId, pageable)).thenReturn(postPage);

        // when
        Page<Post> result = postService.findByTagId(tagId, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        verify(postRepository).findByTagsId(tagId, pageable);
    }

    @Test
    void findById_ShouldReturnPost() {
        // given
        when(postRepository.findById(1L)).thenReturn(Optional.of(post1));

        // when
        Optional<Post> result = postService.findById(1L);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("First Post");
        verify(postRepository).findById(1L);
    }

    @Test
    void findByIdWithComments_ShouldReturnPostWithComments() {
        // given
        when(postRepository.findByIdWithComments(1L)).thenReturn(Optional.of(post1));

        // when
        Optional<Post> result = postService.findByIdWithComments(1L);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("First Post");
        verify(postRepository).findByIdWithComments(1L);
    }

    @Test
    void save_ShouldPersistPost() {
        // given
        when(postRepository.save(any(Post.class))).thenReturn(post1);

        // when
        Post result = postService.save(post1);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("First Post");
        verify(postRepository).save(post1);
    }

    @Test
    void delete_ShouldRemovePost() {
        // given
        Long postId = 1L;
        doNothing().when(postRepository).deleteById(postId);

        // when
        postService.delete(postId);

        // then
        verify(postRepository).deleteById(postId);
    }
}
