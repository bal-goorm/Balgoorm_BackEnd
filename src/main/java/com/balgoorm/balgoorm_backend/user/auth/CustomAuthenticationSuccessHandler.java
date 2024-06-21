package com.balgoorm.balgoorm_backend.user.auth;


import com.balgoorm.balgoorm_backend.user.model.entity.UserRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final ObjectMapper objectMapper;

    public CustomAuthenticationSuccessHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long id = userDetails.getUser().getId(); // 식별자
        UserRole role = userDetails.getUser().getRole(); // 유저 ROLE

        response.setStatus(HttpServletResponse.SC_OK);
        Map<String, String> result = new HashMap<>();
        result.put("message", "로그인 성공");
        result.put("userId", authentication.getName());

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
