package cc.nilm.blog.service;

import cc.nilm.blog.entity.Role;
import cc.nilm.blog.entity.User;
import cc.nilm.blog.repository.RoleRepository;
import cc.nilm.blog.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public User registerUser(User user, boolean isAdmin) {
        // 加密密碼
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 設置角色
        Set<Role> roles = new HashSet<>();
        roleRepository.findByName(Role.ERole.ROLE_USER).ifPresent(roles::add);

        if (isAdmin) {
            roleRepository.findByName(Role.ERole.ROLE_ADMIN).ifPresent(roles::add);
        }

        user.setRoles(roles);
        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(User user) {
        userRepository.deleteById(user.getId());
    }
}