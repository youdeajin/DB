package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.lang.NonNull;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/**")
                // 🚨 allowCredentials(true)를 사용할 때는 와일드카드(*) 대신 명시적인 origin을 지정해야 합니다
                .allowedOrigins(
                    "https://localhost:3000",
                    "http://localhost:3000",
                    "http://localhost:3001"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600); // Preflight 요청 캐시 시간 (1시간)
    }
}