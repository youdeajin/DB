package com.example.demo.controller;

import com.example.demo.dto.UserJoinRequest;
import com.example.demo.dto.UserLoginRequest;
import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final UserService userService;

    @PostMapping("/join")
    public ResponseEntity<String> join(@RequestBody UserJoinRequest request) {
        try {
            userService.join(request);
            return ResponseEntity.status(HttpStatus.CREATED).body("회원가입 성공!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("회원가입 중 서버 오류가 발생했습니다.");
        }
    }

    @PostMapping("/login")
    // 🚨 [수정] Map<String, Object>로 변경하여 Long 타입인 userId를 포함할 수 있게 함
    public ResponseEntity<Map<String, Object>> login(@RequestBody UserLoginRequest request) { 
        try {
            User user = userService.login(request);
            
            Map<String, Object> response = new HashMap<>(); 
            response.put("message", "로그인 성공!");
            response.put("nickname", user.getNickname());
            response.put("email", user.getEmail());
            response.put("userId", user.getUserId()); // Long 타입 포함 가능
            
            return ResponseEntity.ok().body(response);
        } catch (IllegalArgumentException e) {
            // 실패 시는 String 메시지를 Map에 담아 반환
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage())); 
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "로그인 중 서버 오류가 발생했습니다."));
        }
    }
}