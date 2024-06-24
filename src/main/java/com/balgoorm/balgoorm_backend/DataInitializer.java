package com.balgoorm.balgoorm_backend;

import com.balgoorm.balgoorm_backend.user.model.entity.User;
import com.balgoorm.balgoorm_backend.user.model.entity.UserRole;
import com.balgoorm.balgoorm_backend.user.repository.UserRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Configuration
public class DataInitializer {

    @Bean
    public ApplicationRunner initializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (!userRepository.findByUserId("admin").isPresent()) {
                User adminUser = User.builder()
                        .userId("admin")
                        .userPassword(passwordEncoder.encode("admin123"))
                        .nickname("Administrator")
                        .email("admin@example.com")
                        .createDate(LocalDateTime.now())
                        .role(UserRole.ADMIN)
                        .build();
                userRepository.save(adminUser);
            }
        };
    }
}
