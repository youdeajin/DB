package com.example.demo.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider implements InitializingBean {

    @Value("${jwt.secret}")
    private String secretKey;

    private final long tokenValidityInMilliseconds = 3600000; // 1시간

    public JwtTokenProvider() {}
    
    // Spring Bean 초기화 후 시크릿 키가 설정되었는지 검증 (InitializingBean 인터페이스 사용)
    @Override
    public void afterPropertiesSet() {
        if (secretKey == null || secretKey.length() < 32) {
            throw new IllegalStateException("JWT secret key must be set in application.properties and must be at least 32 characters long for HS256.");
        }
    }
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    // 1. 토큰 생성
    public String createToken(Long userId) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + tokenValidityInMilliseconds);

        return Jwts.builder()
                .setSubject(userId.toString()) // 토큰 주체: 사용자 ID
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    // 🚨 2. 토큰에서 사용자 ID(Subject) 추출 (필터에서 사용)
    public String getUserIdFromToken(String token) {
        // 토큰 파싱 중 발생하는 오류는 필터에서 처리합니다.
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // 🚨 3. 토큰 유효성 검증 (필터에서 사용 - 오류 발생 지점)
    public boolean validateToken(String token) {
        try {
            // 토큰 파싱에 성공하면 유효한 토큰
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            // 서명 오류, 만료 오류 등 모든 예외를 잡고 false 반환
            return false;
        }
    }
}