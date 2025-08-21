package cc.nilm.blog.service;

import cc.nilm.blog.entity.Category;
import cc.nilm.blog.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category category1;
    private Category category2;

    @BeforeEach
    public void setUp() {
        category1 = new Category(1L, "AAA", "BBB", null);
        category2 = new Category(2L, "CCC", "DDD", null);
    }

    @Test
    void findAll() {
        List<Category> categories = List.of(category1, category2);
        when(categoryRepository.findAll()).thenReturn(categories);
        List<Category> result = categoryService.findAll();
        assertThat(result).contains(category1).contains(category2).hasSize(2);
    }

    @Test
    void findById() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category1));
        assertThat(categoryService.findById(1L)).isPresent().get().isEqualTo(category1);
    }
}
