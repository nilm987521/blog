package cc.nilm.blog.service;

import cc.nilm.blog.entity.Role;
import cc.nilm.blog.entity.User;
import cc.nilm.blog.repository.RoleRepository;
import cc.nilm.blog.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setup() {
        // 創建測試用戶
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setActive(true);

        // 創建測試角色
        Role roleUser = new Role();
        roleUser.setId(1L);
        roleUser.setName(Role.ERole.valueOf("ROLE_USER"));

        Set<Role> roles = new HashSet<>();
        roles.add(roleUser);
        user.setRoles(roles);
    }

    @Test
    void findByUsername_ShouldReturnUser() {
        // given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // when
        Optional<User> result = userService.findByUsername("testuser");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void findByEmail_ShouldReturnUser() {
        // given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        // when
        Optional<User> result = userService.findByEmail("test@example.com");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("testuser");
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void existsByUsername_ShouldReturnTrue() {
        // given
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // when
        boolean result = userService.existsByUsername("testuser");

        // then
        assertThat(result).isTrue();
        verify(userRepository).existsByUsername("testuser");
    }

    @Test
    void existsByEmail_ShouldReturnTrue() {
        // given
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // when
        boolean result = userService.existsByEmail("test@example.com");

        // then
        assertThat(result).isTrue();
        verify(userRepository).existsByEmail("test@example.com");
    }

    @Test
    void findById_ShouldReturnUser() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // when
        Optional<User> result = userService.findById(1L);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("testuser");
        verify(userRepository).findById(1L);
    }

    @Test
    void save_ShouldPersistUser() {
        // given
        when(userRepository.save(user)).thenReturn(user);

        // when
        User result = userService.save(user);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        verify(userRepository).save(user);
    }

    @Test
    void registerNormal_ShouldPersistUser() {
        User user1 = new User();
        user1.setUsername("testuser");
        user1.setPassword("encodedPassword");
        user1.setEmail("dasda");
        user1.setActive(true);
        user1.setFullName("AAAAA");

        when(userRepository.save(user1)).thenReturn(user1);
        when(passwordEncoder.encode(user1.getPassword())).thenReturn("encodedPassword");
        when(roleRepository.findByName(Role.ERole.ROLE_USER)).thenReturn(Optional.of(new Role(2L, Role.ERole.ROLE_USER)));

        User result = userService.registerUser(user1, false);
        assertThat(result).isEqualTo(user1);
        assertThat(result.getRoles()).hasSize(1);
    }

    @Test
    void registerAdmin_ShouldPersistUser() {
        User user2 = new User();
        user2.setUsername("testuser");
        user2.setPassword("encodedPassword");
        user2.setEmail("dasda");
        user2.setActive(true);
        user2.setFullName("AAAAA");

        when(userRepository.save(user2)).thenReturn(user2);
        when(passwordEncoder.encode(user2.getPassword())).thenReturn("encodedPassword");
        when(roleRepository.findByName(Role.ERole.ROLE_ADMIN)).thenReturn(Optional.of(new Role(1L, Role.ERole.ROLE_ADMIN)));
        when(roleRepository.findByName(Role.ERole.ROLE_USER)).thenReturn(Optional.of(new Role(2L, Role.ERole.ROLE_USER)));

        User result = userService.registerUser(user2, true);
        assertThat(result).isEqualTo(user2);
        assertThat(result.getRoles()).hasSize(2);
    }

    @Test
    void findAll_ShouldReturnUsers() {
        List<User> users = new ArrayList<>();
        User user1 = new User();
        User user2 = new User();
        users.add(user1);
        users.add(user2);
        when(userRepository.findAll()).thenReturn(users);

        List<User> find = userService.findAllUsers();
        assertThat(find).contains(user1).contains(user2).hasSize(2);
    }

    @Test
    void updateUserTest() {
        User user123 = new User();
        when(userRepository.save(user123)).thenReturn(user123);
        User result = userService.updateUser(user123);
        assertThat(result).isNotNull();
    }

    @Test
    void deleteById_ShouldDeleteUser() {
        User user456 = new User();
        userService.deleteUser(user456);
        // 驗證 userRepository.deleteById 是否被呼叫了一次，且參數正確
        verify(userRepository, times(1)).deleteById(user456.getId());
    }

}
