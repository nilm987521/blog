package cc.nilm.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryDto {

    @NotBlank
    @Size(min = 2, max = 30)
    private String name;

    private String description;
}