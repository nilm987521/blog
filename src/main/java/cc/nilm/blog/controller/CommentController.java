package cc.nilm.blog.controller;

import cc.nilm.blog.dto.CommentDto;
import cc.nilm.blog.entity.Comment;
import cc.nilm.blog.entity.Post;
import cc.nilm.blog.entity.User;
import cc.nilm.blog.security.UserDetailsImpl;
import cc.nilm.blog.service.CommentService;
import cc.nilm.blog.service.PostService;
import cc.nilm.blog.service.UserService;
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

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final PostService postService;
    private final UserService userService;

    @GetMapping("/post/{postId}")
    public ResponseEntity<Page<Comment>> getCommentsByPost(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Comment> comments = commentService.findAllByPostId(postId, pageable);

        return ResponseEntity.ok(comments);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<Comment>> getCommentsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Comment> comments = commentService.findAllByUserId(userId, pageable);

        return ResponseEntity.ok(comments);
    }

    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Comment> createComment(
            @Valid @RequestBody CommentDto commentDto,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        User user = userService.findById(currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Post post = postService.findById(commentDto.getPostId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        Comment comment = new Comment();
        comment.setContent(commentDto.getContent());
        comment.setCreatedAt(LocalDateTime.now());
        comment.setAuthor(user);
        comment.setPost(post);

        // 如果是回覆其他評論
        if (commentDto.getParentId() != null) {
            Comment parent = commentService.findById(commentDto.getParentId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parent comment not found"));
            comment.setParent(parent);
        }

        Comment savedComment = commentService.save(comment);
        return new ResponseEntity<>(savedComment, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Page<Comment>> getCommentsByCurrentUser(@AuthenticationPrincipal UserDetailsImpl currentUser) {
        User user = userService.findById(currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        Page<Comment> result = commentService.findAllByUserId(user.getId(), PageRequest.of(0, 10));
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Comment> updateComment(
            @PathVariable Long id,
            @Valid @RequestBody CommentDto commentDto,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        Comment comment = commentService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));

        // 檢查權限 - 只有評論作者或管理員才能編輯
        if (!comment.getAuthor().getId().equals(currentUser.getId()) &&
                currentUser.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        comment.setContent(commentDto.getContent());

        Comment updatedComment = commentService.save(comment);
        return ResponseEntity.ok(updatedComment);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteComment(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        Comment comment = commentService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));

        // 檢查權限 - 只有評論作者、文章作者或管理員才能刪除
        if (!comment.getAuthor().getId().equals(currentUser.getId()) &&
                !comment.getPost().getAuthor().getId().equals(currentUser.getId()) &&
                currentUser.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        commentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}