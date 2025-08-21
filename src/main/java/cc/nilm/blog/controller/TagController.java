package cc.nilm.blog.controller;

import cc.nilm.blog.dto.MessageResponse;
import cc.nilm.blog.dto.TagDto;
import cc.nilm.blog.entity.Tag;
import cc.nilm.blog.service.TagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping
    public ResponseEntity<List<Tag>> getAllTags() {
        List<Tag> tags = tagService.findAll();
        return ResponseEntity.ok(tags);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tag> getTagById(@PathVariable Long id) {
        Tag tag = tagService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tag not found"));

        return ResponseEntity.ok(tag);
    }

    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> createTag(@Valid @RequestBody TagDto tagDto) {
        if (tagService.existsByName(tagDto.getName())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Tag name already exists!"));
        }

        Tag tag = new Tag();
        tag.setName(tagDto.getName());
        tag.setColor(tagDto.getColor());

        Tag savedTag = tagService.save(tag);
        return new ResponseEntity<>(savedTag, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateTag(
            @PathVariable Long id,
            @Valid @RequestBody TagDto tagDto) {

        Tag tag = tagService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tag not found"));

        // 檢查名稱是否已存在（如果名稱有變更）
        if (!tag.getName().equals(tagDto.getName()) &&
                tagService.existsByName(tagDto.getName())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Tag name already exists!"));
        }

        tag.setName(tagDto.getName());
        tag.setColor(tagDto.getColor());

        Tag updatedTag = tagService.save(tag);
        return ResponseEntity.ok(updatedTag);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteTag(@PathVariable Long id) {
        tagService.delete(id);
        return ResponseEntity.ok(new MessageResponse("Tag deleted successfully"));
    }
}