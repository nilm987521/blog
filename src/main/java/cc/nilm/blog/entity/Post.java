package cc.nilm.blog.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "posts")
@Schema(description = "文章實體")
@ToString(exclude = {"tags", "comments"})
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "文章ID", example = "1")
    private Long id;

    @Column(nullable = false)
    @Schema(description = "文章標題", example = "Spring Boot 入門")
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    @Schema(description = "文章內容", example = "這篇文章介紹了Spring Boot的基本概念和使用方法...")
    private String content;

    @Column(nullable = false)
    @Schema(description = "創建時間", example = "2025-05-14T10:30:00")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Schema(description = "更新時間", example = "2025-05-15T08:15:00")
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    @Schema(description = "是否發布", example = "true")
    private boolean published = false;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    @Schema(description = "文章作者")
    @JsonIgnoreProperties({"email", "roles", "posts", "comments", "profileImage", "bio", "active"})
//    @JsonBackReference
    private User author;

    @ManyToOne
    @JoinColumn(name = "category_id")
    @Schema(description = "文章分類")
    @JsonBackReference
    private Category category;

    @ManyToMany
    @JoinTable(
            name = "post_tags",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Schema(description = "文章標籤")
    private Set<Tag> tags = new HashSet<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Schema(description = "文章評論")
    private Set<Comment> comments = new HashSet<>();

    @Override
    public int hashCode() {
        // 避免使用 user 和 category 屬性，它們會引起遞歸
        return Objects.hash(id, title, createdAt);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Post other = (Post) obj;
        return Objects.equals(id, other.id);
    }
}