package cc.nilm.blog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
@Schema(description = "文章數據傳輸對象")
public class PostDto {

    @NotBlank
    @Size(min = 3, max = 100)
    @Schema(description = "文章標題", example = "Spring Boot 教程", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @NotBlank
    @Schema(description = "文章內容", example = "這是一篇關於Spring Boot的教程...", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;

    @Schema(description = "是否發布", example = "true", defaultValue = "false")
    private boolean published;

    @Schema(description = "分類ID", example = "1")
    private Long categoryId;

    @Schema(description = "標籤ID集合", example = "[1, 2, 3]")
    private Set<Long> tagIds;
}