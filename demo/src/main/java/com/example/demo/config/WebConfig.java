package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring Security를 사용하지 않고 CORS 설정을 전역으로 적용하기 위한 파일
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 1. 모든 경로(/**)에 대해
                .allowedOriginPatterns("*")   // 2. 모든 출처(Origin)를 허용 (예: localhost:3000)
                .allowedMethods("*")          // 3. 모든 HTTP 메서드(GET, POST, PUT, DELETE 등) 허용
                .allowedHeaders("*")          // 4. 모든 헤더 허용
                .allowCredentials(true);      // 5. 자격 증명(쿠키 등) 허용
    }
}