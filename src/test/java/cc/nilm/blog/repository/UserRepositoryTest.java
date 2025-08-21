package cc.nilm.blog.repository;

import cc.nilm.blog.entity.User;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    private final User user = new User();

    @BeforeEach
    void setUp() {
        user.setUsername("admin2");
        user.setPassword("123456");
        user.setEmail("xxx@gmail.com");
        user.setFullName("ADMIN2");
        entityManager.persist(user);
        entityManager.flush();
    }


    @Test
    void findByUsernameTest() {
        Optional<User> find = userRepository.findByUsername("admin2");
        Assertions.assertThat(find).isPresent();
    }

    @Test
    void findByIdTest() {
        long userId = user.getId();
        Optional<User> find = userRepository.findById(userId);
        Assertions.assertThat(find).isPresent();
    }

    @Test
    void findByEmailTest() {
        String email = user.getEmail();
        Optional<User> find = userRepository.findByEmail(email);
        Assertions.assertThat(find).isPresent();
    }

    @Test
    void findByFullNameTest() {
        Optional<User> find = userRepository.findByUsername(user.getUsername());
        Assertions.assertThat(find).isPresent();
    }

    @Test
    void existsByUsernameTest() {
        boolean result = userRepository.existsByUsername(user.getUsername());
        Assertions.assertThat(result).isTrue();
    }


    @Test
    void deleteTest() {
        User user1 = new User();
        user1.setUsername("admin3");
        user1.setPassword("123456");
        user1.setFullName("ADMIN3");
        user1.setEmail("yyy@gmail.com");
        entityManager.persist(user1);
        userRepository.delete(user1);
        Optional<User> find = userRepository.findById(user1.getId());
        Assertions.assertThat(find).isEmpty() ;
    }
}
