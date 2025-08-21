package cc.nilm.blog.repository;

import cc.nilm.blog.entity.Tag;
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
class TagRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TagRepository tagRepository;

    private Tag tag1;

    @BeforeEach
    void setup() {
        // 創建測試標籤
        tag1 = new Tag();
        tag1.setName("Java");
        entityManager.persist(tag1);

        Tag tag2 = new Tag();
        tag2.setName("Spring");
        entityManager.persist(tag2);

        entityManager.flush();
    }

    @Test
    void findById_ShouldReturnTag() {
        // when
        Optional<Tag> found = tagRepository.findById(tag1.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Java");
    }

    @Test
    void findAll_ShouldReturnAllTags() {
        // when
        List<Tag> tags = tagRepository.findAll();

        // then
        assertThat(tags).hasSize(2);
        assertThat(tags).extracting(Tag::getName).containsExactlyInAnyOrder("Java", "Spring");
    }

    @Test
    void save_ShouldPersistTag() {
        // given
        Tag newTag = new Tag();
        newTag.setName("Hibernate");

        // when
        Tag savedTag = tagRepository.save(newTag);

        // then
        Tag found = entityManager.find(Tag.class, savedTag.getId());
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Hibernate");
    }

    @Test
    void update_ShouldModifyExistingTag() {
        // given
        Tag tag = entityManager.find(Tag.class, tag1.getId());
        tag.setName("Updated Java");

        // when
        tagRepository.save(tag);
        entityManager.flush();
        entityManager.clear();

        // then
        Tag found = entityManager.find(Tag.class, tag1.getId());
        assertThat(found.getName()).isEqualTo("Updated Java");
    }

    @Test
    void deleteById_ShouldRemoveTag() {
        // when
        tagRepository.deleteById(tag1.getId());
        entityManager.flush();
        entityManager.clear();

        // then
        Tag found = entityManager.find(Tag.class, tag1.getId());
        assertThat(found).isNull();
    }

    @Test
    void findByName_ShouldReturnMatchingTag() {
        // when
        Optional<Tag> found = tagRepository.findByName("Java");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(tag1.getId());
    }
}
