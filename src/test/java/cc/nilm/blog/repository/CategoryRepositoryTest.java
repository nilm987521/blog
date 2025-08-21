package cc.nilm.blog.repository;

import cc.nilm.blog.entity.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class CategoryRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category category1;

    @BeforeEach
    void setup() {
        // 創建測試分類
        category1 = new Category();
        category1.setName("Technology");
        entityManager.persist(category1);

        Category category2 = new Category();
        category2.setName("Lifestyle");
        entityManager.persist(category2);

        entityManager.flush();
    }

    @Test
    void findById_ShouldReturnCategory() {
        // when
        Optional<Category> found = categoryRepository.findById(category1.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Technology");
    }

    @Test
    void findAll_ShouldReturnAllCategories() {
        // when
        List<Category> categories = categoryRepository.findAll();

        // then
        assertThat(categories).hasSize(2);
        assertThat(categories).extracting(Category::getName).containsExactlyInAnyOrder("Technology", "Lifestyle");
    }

    @Test
    void save_ShouldPersistCategory() {
        // given
        Category newCategory = new Category();
        newCategory.setName("Programming");

        // when
        Category savedCategory = categoryRepository.save(newCategory);

        // then
        Category found = entityManager.find(Category.class, savedCategory.getId());
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Programming");
    }

    @Test
    void update_ShouldModifyExistingCategory() {
        // given
        Category category = entityManager.find(Category.class, category1.getId());
        category.setName("Updated Technology");

        // when
        categoryRepository.save(category);
        entityManager.flush();
        entityManager.clear();

        // then
        Category found = entityManager.find(Category.class, category1.getId());
        assertThat(found.getName()).isEqualTo("Updated Technology");
    }

    @Test
    void deleteById_ShouldRemoveCategory() {
        // when
        categoryRepository.deleteById(category1.getId());
        entityManager.flush();
        entityManager.clear();

        // then
        Category found = entityManager.find(Category.class, category1.getId());
        assertThat(found).isNull();
    }

    @Test
    void findByName_ShouldReturnMatchingCategory() {
        // when
        Optional<Category> found = categoryRepository.findByName("Technology");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(category1.getId());
    }
}
