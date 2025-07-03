package com.balgoorm.balgoorm_backend.user.auth;

import com.balgoorm.balgoorm_backend.user.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Spring Security 인증 객체(Principal) 역할
 * - 인증된 사용자 정보를 UserDetails로 래핑
 * - 권한, 계정상태 등 인증 관련 정보를 제공
 */
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    // 인증된 유저 엔티티
    private final User user;

    /**
     * 사용자 권한 반환 (ex: ROLE_USER, ROLE_ADMIN)
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );
    }

    /**
     * 인증 객체에서 User 엔티티 직접 반환 (편의 메서드)
     */
    public User getUser() {
        return user;
    }

    /** 패스워드 반환 */
    @Override
    public String getPassword() {
        return user.getUserPassword();
    }

    /** username 반환 (로그인시 사용하는 아이디) */
    @Override
    public String getUsername() {
        return user.getUserId();
    }

    /** 계정 만료 여부 (true: 만료X) */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /** 계정 잠김 여부 (true: 잠김X) */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /** 인증정보 만료 여부 (true: 만료X) */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /** 활성화 여부 (true: 활성화) */
    @Override
    public boolean isEnabled() {
        return true;
    }

    /** User 엔티티의 PK 반환 (추가 편의 메서드) */
    public Long getUserId() {
        return user.getId();
    }
}
