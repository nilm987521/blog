package cc.nilm.blog.controller;

import cc.nilm.blog.dto.PostDto;
import cc.nilm.blog.entity.*;
import cc.nilm.blog.security.UserDetailsImpl;
import cc.nilm.blog.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(name = "文章管理", description = "文章的CRUD操作及相關查詢")
public class PostController {

    private final PostService postService;
    private final UserService userService;
    private final CategoryService categoryService;
    private final TagService tagService;
    private final CommentService commentService;

    @GetMapping
    @Operation(summary = "獲取所有已發布的文章", description = "分頁獲取所有已發布的文章，可指定排序方式")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功獲取文章列表", 
                    content = @Content(schema = @Schema(implementation = Post.class)))
    })
    public ResponseEntity<Page<Post>> getAllPosts(
            @Parameter(description = "頁碼，從0開始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每頁大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "排序欄位") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "排序方向：asc升序，desc降序") @RequestParam(defaultValue = "desc") String direction) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<Post> posts = postService.findPublishedPosts(pageable);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根據ID獲取文章", description = "獲取指定ID的文章詳細資訊")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功獲取文章",
                    content = @Content(schema = @Schema(implementation = Post.class))),
        @ApiResponse(responseCode = "404", description = "文章不存在")
    })
    public ResponseEntity<Post> getPostById(
            @Parameter(description = "文章ID", required = true) @PathVariable Long id) {
        Post post = postService.findByIdWithComments(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
        // FIXME: OneToMany 都是空集合
        if (post.getComments().isEmpty()) {
            Page<Comment> commentPage = commentService.findAllByPostId(post.getId(), PageRequest.of(0, 10));
            if (!commentPage.getContent().isEmpty()) post.setComments(commentPage.toSet());
        }
        return ResponseEntity.ok(post);
    }

    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "創建新文章", description = "創建一篇新的文章，需要用戶或管理員權限")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "文章創建成功",
                    content = @Content(schema = @Schema(implementation = Post.class))),
        @ApiResponse(responseCode = "400", description = "提交的數據無效"),
        @ApiResponse(responseCode = "403", description = "沒有權限"),
        @ApiResponse(responseCode = "404", description = "引用的資源（分類或標籤）不存在")
    })
    public ResponseEntity<Post> createPost(
            @Valid @RequestBody PostDto postDto,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        User author = userService.findById(currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Post post = new Post();
        post.setTitle(postDto.getTitle());
        post.setContent(postDto.getContent());
        post.setPublished(postDto.isPublished());
        post.setAuthor(author);
        post.setCreatedAt(LocalDateTime.now());

        // 設置分類
        if (postDto.getCategoryId() != null) {
            Category category = categoryService.findById(postDto.getCategoryId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
            post.setCategory(category);
        }

        // 設置標籤
        if (postDto.getTagIds() != null && !postDto.getTagIds().isEmpty()) {
            Set<Tag> tags = new HashSet<>();
            for (Long tagId : postDto.getTagIds()) {
                Tag tag = tagService.findById(tagId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tag not found"));
                tags.add(tag);
            }
            post.setTags(tags);
        }

        Post savedPost = postService.save(post);
        return new ResponseEntity<>(savedPost, HttpStatus.CREATED);
    }

    @PostMapping("/draft")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "創建草稿文章", description = "創建一篇空的草稿文章，用於後續編輯")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "草稿創建成功",
                    content = @Content(schema = @Schema(implementation = Post.class))),
        @ApiResponse(responseCode = "403", description = "沒有權限")
    })
    public ResponseEntity<Post> createDraftPost(@AuthenticationPrincipal UserDetailsImpl currentUser) {
        User author = userService.findById(currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Post post = new Post();
        post.setTitle("新文章草稿");
        post.setContent("");
        post.setPublished(false);
        post.setAuthor(author);
        post.setCreatedAt(LocalDateTime.now());

        Post savedPost = postService.save(post);
        return new ResponseEntity<>(savedPost, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Post> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody PostDto postDto,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        Post post = postService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        // 檢查權限 - 只有作者或管理員才能編輯
        if (!post.getAuthor().getId().equals(currentUser.getId()) &&
                currentUser.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        post.setTitle(postDto.getTitle());
        post.setContent(postDto.getContent());
        post.setPublished(postDto.isPublished());
        post.setUpdatedAt(LocalDateTime.now());

        // 更新分類
        if (postDto.getCategoryId() != null) {
            Category category = categoryService.findById(postDto.getCategoryId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
            post.setCategory(category);
        } else {
            post.setCategory(null);
        }

        // 更新標籤
        if (postDto.getTagIds() != null) {
            Set<Tag> tags = new HashSet<>();
            for (Long tagId : postDto.getTagIds()) {
                Tag tag = tagService.findById(tagId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tag not found"));
                tags.add(tag);
            }
            post.setTags(tags);
        } else {
            post.setTags(new HashSet<>());
        }

        Post updatedPost = postService.save(post);
        return ResponseEntity.ok(updatedPost);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> deletePost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        Post post = postService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        // 檢查權限 - 只有作者或管理員才能刪除
        if (!post.getAuthor().getId().equals(currentUser.getId()) &&
                currentUser.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        postService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<Post>> getPostsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        User user = userService.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Post> posts = postService.findByAuthor(user, pageable);

        return ResponseEntity.ok(posts);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<Post>> getPostsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Post> posts = postService.findByCategoryId(categoryId, pageable);

        return ResponseEntity.ok(posts);
    }

    @GetMapping("/tag/{tagId}")
    public ResponseEntity<Page<Post>> getPostsByTag(
            @PathVariable Long tagId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Post> posts = postService.findByTagId(tagId, pageable);

        return ResponseEntity.ok(posts);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<Post>> searchPosts(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Post> posts = postService.searchPosts(query, pageable);

        return ResponseEntity.ok(posts);
    }
}