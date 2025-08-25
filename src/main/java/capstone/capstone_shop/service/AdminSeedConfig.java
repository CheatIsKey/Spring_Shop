package capstone.capstone_shop.service;

import capstone.capstone_shop.domain.Address;
import capstone.capstone_shop.domain.User;
import capstone.capstone_shop.domain.UserRole;
import capstone.capstone_shop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class AdminSeedConfig {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner Admin() {
        return args -> {
            String adminLoginId = "admin";
            boolean exists = userRepository.existsByIdUser(adminLoginId);
            if (exists) return;

            String adminPW = passwordEncoder.encode("admin");

            User admin = User.createUser(
                    "관리자",
                    "010-1234-5678",
                    adminLoginId,
                    adminPW,
                    new Address("Test", "sever", "Script"),
                    UserRole.ADMIN
            );

            userRepository.save(admin);
        };
    }
}
