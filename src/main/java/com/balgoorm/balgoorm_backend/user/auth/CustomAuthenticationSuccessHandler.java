package com.balgoorm.balgoorm_backend.user.auth;

import com.balgoorm.balgoorm_backend.user.model.entity.UserRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Spring Security 로그인 성공시 실행되는 커스텀 핸들러
 * - 성공 응답을 JSON 구조로 반환
 */
@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final ObjectMapper objectMapper;

    // ObjectMapper는 Spring에서 자동 주입 받음
    public CustomAuthenticationSuccessHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 로그인 성공 시 호출되는 메서드
     * - 사용자 PK, ROLE 정보를 JSON으로 반환
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        // 인증된 사용자 정보 추출
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long id = userDetails.getUser().getId();         // 사용자 PK
        UserRole role = userDetails.getUser().getRole(); // 사용자 권한

        // 응답 구조 정의 (실무에서는 키 이름 통일, 타입 맞추는 것 중요!)
        Map<String, Object> result = new HashMap<>();
        result.put("id", id);            // 숫자 그대로 전달
        result.put("role", role.name()); // Enum값 문자열로

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        // JSON으로 변환하여 응답
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
