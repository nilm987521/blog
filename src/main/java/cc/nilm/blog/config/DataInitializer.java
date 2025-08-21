package cc.nilm.blog.config;

import cc.nilm.blog.entity.Role;
import cc.nilm.blog.entity.User;
import cc.nilm.blog.repository.RoleRepository;
import cc.nilm.blog.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserService userService;

    @Override
    public void run(String... args) throws Exception {
        initRoles();
        initAdminUser();
    }

    private void initRoles() {
        log.info("Initializing roles...");

        if (roleRepository.count() == 0) {
            Role userRole = new Role();
            userRole.setName(Role.ERole.ROLE_USER);
            roleRepository.save(userRole);

            Role adminRole = new Role();
            adminRole.setName(Role.ERole.ROLE_ADMIN);
            roleRepository.save(adminRole);

            log.info("Roles initialized successfully");
        }
    }

    private void initAdminUser() {
        log.info("Initializing admin user...");

        if (!userService.existsByUsername("admin")) {
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setEmail("admin@example.com");
            adminUser.setPassword("admin123"); // 應該使用更強的密碼
            adminUser.setFullName("System Admin");
            adminUser.setActive(true);

            userService.registerUser(adminUser, true);

            log.info("Admin user initialized successfully");
        }
    }
}