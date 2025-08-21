package cc.nilm.blog.service;

import cc.nilm.blog.entity.Tag;
import cc.nilm.blog.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private TagService tagService;

    private Tag tag1;
    private Tag tag2;

    @BeforeEach
    void setUp() {
        tag1 = new Tag();
        tag1.setName("AAA");
        tag2 = new Tag();
        tag2.setName("BBB");
    }

    @Test
    void findAll() {
        List<Tag> tags = Arrays.asList(tag1, tag2);
        when(tagRepository.findAll()).thenReturn(tags);
        List<Tag> result = tagService.findAll();
        assertThat(result).contains(tag1).contains(tag2).hasSize(2);
    }

    @Test
    void findById() {
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag1));
        assertThat(tagService.findById(1L)).isPresent().get().isEqualTo(tag1);
    }

    @Test
    void findByName() {
        when(tagRepository.findByName(tag1.getName())).thenReturn(Optional.of(tag1));
        assertThat(tagService.findByName(tag1.getName())).isPresent().get().isEqualTo(tag1);
    }

    @Test
    void existsByName() {
        when(tagRepository.existsByName(tag1.getName())).thenReturn(true);
        assertThat(tagService.existsByName(tag1.getName())).isTrue();
    }

    @Test
    void save() {
        when(tagRepository.save(tag1)).thenReturn(tag1);
        assertThat(tagService.save(tag1)).isEqualTo(tag1);
    }

    @Test
    void delete() {
        tagService.delete(1L);
        verify(tagRepository, times(1)).deleteById(1L);
    }
}
