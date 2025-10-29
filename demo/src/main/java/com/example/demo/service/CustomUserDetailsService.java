package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.core.authority.SimpleGrantedAuthority; // 🚨 권한 부여용 import

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    // JWT는 userId로 인증하므로, email을 사용하는 이 메서드는 사용하지 않습니다.
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        throw new UnsupportedOperationException("JWT system uses userId, not email for authentication loading.");
    }
    
    // 🚨 JWT 필터에서 userId를 사용하여 UserDetails를 반환할 커스텀 메서드
    public UserDetails loadUserById(Long userId) throws UsernameNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));
        
        // CustomUserDetail 객체를 생성하여 반환합니다.
        return new CustomUserDetail(user);
    }

    // 🚨 UserDetails를 구현할 내부 클래스
    public static class CustomUserDetail implements UserDetails {
        private final User user;

        public CustomUserDetail(User user) {
            this.user = user;
        }

        // 인증 필터에서 권한을 사용하지 않으므로 임시로 빈 컬렉션 반환
        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() { 
            // 기본 권한으로 ROLE_USER를 부여합니다.
            Collection<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            return authorities;
        }
        
       @Override
        public String getPassword() { 
    // User 엔티티의 getPassword()를 호출합니다.
        return user.getPassword(); 
}
        @Override
        public String getUsername() { return user.getEmail(); } // username 대신 email 사용
        @Override
        public boolean isAccountNonExpired() { return true; }
        @Override
        public boolean isAccountNonLocked() { return true; }
        @Override
        public boolean isCredentialsNonExpired() { return true; }
        @Override
        public boolean isEnabled() { return true; }
        
        public Long getUserId() { return user.getUserId(); }
    }
}
