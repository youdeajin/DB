package com.example.demo.config;

// 🚨 JWT 관련 import 제거
// import com.example.demo.jwt.JwtAuthenticationFilter;
// import com.example.demo.jwt.JwtTokenProvider;
// import com.example.demo.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
// 🚨 필터 등록 관련 import 제거
// import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // 🚨 JWT 관련 필드 제거
    // private final JwtTokenProvider jwtTokenProvider;
    // private final CustomUserDetailsService customUserDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 🚨 JWT 필터 빈 생성 메서드 제거
    // @Bean
    // public JwtAuthenticationFilter jwtAuthenticationFilter() { ... }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(c -> c.disable()) // CORS는 CorsFilter로 처리
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 비활성화 유지
            
            .authorizeHttpRequests(authorize -> authorize
                // 🚨 모든 경로를 인증 없이 허용 (permitAll)
                .requestMatchers("/**").permitAll() 
                // .anyRequest().authenticated() // 인증 요구 제거
            );

        http.formLogin(AbstractHttpConfigurer::disable); // 폼 로그인 비활성화 유지

        // 🚨 JWT 필터 등록 제거
        // http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);


        return http.build();
    }
    
    // CORS 필터 설정 (기존 코드 유지)
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*"); // 모든 출처 허용
        config.addAllowedHeader("*");        // 모든 헤더 허용
        config.addAllowedMethod("*");        // 모든 HTTP 메서드 허용
        
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}

