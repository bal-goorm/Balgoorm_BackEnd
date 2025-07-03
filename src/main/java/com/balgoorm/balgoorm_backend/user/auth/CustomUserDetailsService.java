package com.balgoorm.balgoorm_backend.user.auth;

import com.balgoorm.balgoorm_backend.user.model.entity.User;
import com.balgoorm.balgoorm_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Spring Security 인증을 위한 UserDetailsService 구현체
 * - 로그인 시 DB에서 유저 정보를 조회하여 UserDetails(Principal)로 반환
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * username(userId)로 DB에서 User 엔티티 조회
     * @param username 프론트에서 전달한 userId (스프링 시큐리티는 username으로 전달)
     * @return 인증에 사용할 UserDetails 구현체
     * @throws UsernameNotFoundException 유저를 찾지 못하면 예외 발생
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // userId로 유저를 찾고, 없으면 예외 발생
        User user = userRepository.findByUserId(username)
                .orElseThrow(() -> new UsernameNotFoundException("회원 정보를 찾을 수 없습니다."));
        // 인증에 사용할 UserDetails 객체로 변환하여 반환
        return new CustomUserDetails(user);
    }
}
