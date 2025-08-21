package cc.nilm.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentDto {

    @NotBlank
    private String content;

    @NotNull
    private Long postId;

    private Long parentId;
}