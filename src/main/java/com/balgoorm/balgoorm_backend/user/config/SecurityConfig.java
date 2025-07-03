package com.balgoorm.balgoorm_backend.user.config;

import com.balgoorm.balgoorm_backend.user.auth.CustomAuthenticationSuccessHandler;
import com.balgoorm.balgoorm_backend.user.auth.CustomUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.stereotype.Component;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

@Configuration // 스프링 빈 등록(설정 클래스)
@EnableWebSecurity // 스프링 시큐리티 활성화
public class SecurityConfig {

    // 사용자 인증정보 및 로그인 성공 핸들러 주입
    private final CustomUserDetailsService customUserDetailsService;
    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    // 생성자 주입 (의존성)
    public SecurityConfig(CustomUserDetailsService customUserDetailsService, CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler) {
        this.customUserDetailsService = customUserDetailsService;
        this.customAuthenticationSuccessHandler = customAuthenticationSuccessHandler;
    }

    // 비밀번호 암호화 전략(BCrypt) 등록
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // CORS 정책 설정 (프론트와 API 서버 도메인 분리 시 필요)
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 운영 환경에서는 * 대신 실제 도메인 명시!
        configuration.setAllowedOriginPatterns(Arrays.asList("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET","POST","PUT","DELETE"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // 보안 정책의 핵심: SecurityFilterChain Bean 등록 (스프링 시큐리티 5.7 이상)
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                // CSRF(위조공격) 방지 비활성화 (API 서버라면 대부분 사용)
                .csrf(AbstractHttpConfigurer::disable)
                // CORS 정책 적용
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // URL 패턴별 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/img/**", "/font/**", "/error").permitAll() // 정적 자원/에러페이지 모두 허용
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")                                // 관리자 권한 필요
                        .requestMatchers("/api/myinfo/**").hasAnyRole("ADMIN","USER")                    // 관리자/일반유저 모두 접근 가능
                        .requestMatchers("/api/login", "/api/signup", "/api/logout", "/api/test/**").permitAll() // 인증/회원가입/로그아웃 허용
                        .anyRequest().hasAnyRole("ADMIN", "USER") // 그 외 모든 요청은 인증 필요
                )
                // 폼 로그인 방식(백엔드 단독 로그인 API 처리)
                .formLogin(login -> login
                        .loginPage("/login")                          // (REST라면 의미 없음, 프론트엔드에서 폼 제공)
                        .loginProcessingUrl("/api/login")             // 실제 로그인 요청 API
                        .successHandler(customAuthenticationSuccessHandler) // 로그인 성공시 커스텀 동작
                        .failureHandler((request, response, exception) -> { // 로그인 실패 응답
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\": \"Invalid username or password.\"}");
                        })
                        .usernameParameter("userId")                  // 로그인 form에서 id 필드명
                        .passwordParameter("userPassword")            // 로그인 form에서 pw 필드명
                        .permitAll()
                )
                // 로그아웃 설정
                .logout(logout -> logout
                        .logoutUrl("/api/logout")                     // 로그아웃 API 경로
                        .logoutSuccessHandler(customLogoutSuccessHandler()) // 커스텀 핸들러 적용
                        .invalidateHttpSession(true)                  // 세션 무효화
                        .clearAuthentication(true)                    // 인증 정보 삭제
                        .deleteCookies("JSESSIONID")                  // 쿠키 삭제
                )
                // 세션 관리 정책
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) // 세션은 필요할 때만 생성
                        .maximumSessions(1)                                      // 동시 접속 1개 제한
                        .maxSessionsPreventsLogin(false)                         // 중복 로그인 허용
                )
                // 사용자 인증정보 서비스 등록
                .userDetailsService(customUserDetailsService);

        return httpSecurity.build();
    }

    // 로그아웃 성공시 커스텀 핸들러 Bean 등록
    @Bean
    public LogoutSuccessHandler customLogoutSuccessHandler() {
        return new CustomLogoutSuccessHandler();
    }

    // 실제 로그아웃 성공시 JSON 메시지 반환 (내부 static class)
    @Component
    public static class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

        private final ObjectMapper objectMapper = new ObjectMapper();

        @Override
        public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json;charset=UTF-8");
            // {"message": "로그아웃 성공"} 형태로 응답
            response.getWriter().write(objectMapper.writeValueAsString(Map.of("message", "로그아웃 성공")));
            response.getWriter().flush();
        }
    }
}
