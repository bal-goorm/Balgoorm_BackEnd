package com.balgoorm.balgoorm_backend;

import com.balgoorm.balgoorm_backend.user.model.entity.User;
import com.balgoorm.balgoorm_backend.user.model.entity.UserRole;
import com.balgoorm.balgoorm_backend.user.repository.UserRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 서버 시작 시 관리자(admin) 계정이 없으면 자동 생성하는 초기화 클래스
 */
@Configuration
public class DataInitializer {

    // 관리자 계정 상수 선언
    private static final String ADMIN_ID = "admin";
    private static final String ADMIN_PW = "admin123";
    private static final String ADMIN_NICKNAME = "Administrator";
    private static final String ADMIN_EMAIL = "admin@example.com";

    @Bean
    public ApplicationRunner initializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // admin 계정이 없으면 신규 생성
            if (userRepository.findByUserId(ADMIN_ID).isEmpty()) {
                User adminUser = User.builder()
                        .userId(ADMIN_ID)
                        .userPassword(passwordEncoder.encode(ADMIN_PW))
                        .nickname(ADMIN_NICKNAME)
                        .email(ADMIN_EMAIL)
                        .role(UserRole.ADMIN)
                        .build();
                userRepository.save(adminUser);
            }
        };
    }
}
