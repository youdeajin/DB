package com.example.demo.service;

import com.example.demo.dto.AdminAuthRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

    @Value("${admin.password:admin123}") // 기본값: admin123, application.properties에서 설정 가능
    private String adminPassword;

    /**
     * 관리자 비밀번호 인증
     */
    public boolean authenticate(AdminAuthRequest request) {
        return adminPassword.equals(request.getPassword());
    }
}

