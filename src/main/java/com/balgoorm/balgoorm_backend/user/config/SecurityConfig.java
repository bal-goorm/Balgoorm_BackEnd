package com.balgoorm.balgoorm_backend.user.config;

import com.balgoorm.balgoorm_backend.user.auth.CustomAuthenticationSuccessHandler;
import com.balgoorm.balgoorm_backend.user.auth.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity

public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService, CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler) {
        this.customUserDetailsService = customUserDetailsService;
        this.customAuthenticationSuccessHandler = customAuthenticationSuccessHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

//    @Bean
//    public WebSecurityCustomizer webSecurityCustomizer() {
//        // 정적 리소스와 회원가입 경로가 보안필터를 거치지 않게끔 설정
//        return (web) -> web.ignoring().requestMatchers("/css/**", "/js/**", "/img/**", "/font/**");
//    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("http://localhost:3000/","*"));
        configuration.setAllowedMethods(Arrays.asList("GET","POST","PUT"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))                               // CORS 비활성화
                .csrf(csrf -> csrf.disable())                           // CSRF 비활성화
                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers( "/css/**", "/js/**", "/img/**", "/font/**").permitAll()
//                        .requestMatchers("/admin/**").hasRole("ADMIN")
//                        .requestMatchers("/member/**", "/board/**").hasRole("USER")
//                        .requestMatchers("/login","/signup").anonymous()
                                .anyRequest().permitAll() // 모든 요청 허용
                )
                .formLogin(login -> login
                        .loginProcessingUrl("/api/login") // 로그인 폼이 제출될 URL 설정
                        .successHandler(customAuthenticationSuccessHandler) // 로그인 성공 시 커스텀 핸들러
                        .failureUrl("/login.html?error=true") // 실패했을 때 리턴하는 주소
                        .usernameParameter("userId") // 해당 메소드에 적은 userId를 login form에 적힌 userId와 일치 시킴
                        .passwordParameter("password") // 비밀번호 파라미터 설정
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/api/logout") // 로그아웃 URL
                        .logoutSuccessUrl("/login.html?logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                )
                .userDetailsService(customUserDetailsService);

        return httpSecurity.build();
    }
}