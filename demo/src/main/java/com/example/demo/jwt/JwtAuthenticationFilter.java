package com.example.demo.jwt;

import com.example.demo.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String jwt = resolveToken(request);
        
        if (jwt != null) {
            logger.info("JWT found: " + jwt.substring(0, 20) + "..."); // 토큰의 일부만 출력
        } else {
            logger.info("No JWT found in request.");
        }

        if (jwt != null && jwtTokenProvider.validateToken(jwt)) {
            
            try {
                // 1. 토큰에서 사용자 ID(String) 추출
                String userIdString = jwtTokenProvider.getUserIdFromToken(jwt); 
                
                // 2. 추출한 String ID를 Long 타입으로 변환 (NumberFormatException이 발생하는 지점)
                // 🚨 이 변환이 필수적입니다.
                Long userId = Long.valueOf(userIdString); 
                
                // 3. 추출된 ID로 UserDetails 로드
                UserDetails userDetails = customUserDetailsService.loadUserById(userId);
                
                // 4. SecurityContext에 인증 객체 저장
                // ... (나머지 인증 로직 유지)

            } catch (Exception e) {
                // ... (예외 처리 로직 유지)
            }
        }
        else if (jwt != null && !jwtTokenProvider.validateToken(jwt)) {
        // 🚨 토큰이 있지만 유효성 검증에 실패한 경우 로그
        logger.error("JWT validation failed: Invalid or Expired Token"); 
        }

        filterChain.doFilter(request, response);
    }
}