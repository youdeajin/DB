package com.example.demo.service;

import com.example.demo.dto.UserJoinRequest;
import com.example.demo.dto.UserLoginRequest;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.jwt.JwtTokenProvider; // 🚨 JWT Provider import
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider; // 🚨 JWT Provider 주입

  // UserService.java 파일의 join 메서드 일부 수정
    @Transactional
    public User join(UserJoinRequest request) {
        userRepository.findByEmail(request.getEmail())
            .ifPresent(user -> {
                throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
            });

        // 🚨 User.builder() 대신 수동 생성자 사용
        User user = new User(
            request.getEmail(),
            passwordEncoder.encode(request.getPassword()), 
            request.getNickname(),
            LocalDateTime.now()
        );
            
        return userRepository.save(user);
}

    // 🚨 로그인 처리 로직 수정: 토큰 문자열 반환
    @Transactional(readOnly = true)
    public String login(UserLoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("이메일이 일치하지 않습니다."));
        
        // 비밀번호 비교 (생략)
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        
        // 🚨 핵심 수정: 토큰 생성 시 User ID(Long)를 전달해야 합니다.
        // user.getUserId()는 Long 타입입니다.
        return jwtTokenProvider.createToken(user.getUserId()); 
    }
}
