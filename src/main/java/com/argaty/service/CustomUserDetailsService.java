package com.argaty.service;

import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.argaty.entity.User;
import com.argaty.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.info("Attempting to load user by email: {}", email);
        
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> {
                log.warn("User not found with email: {}", email);
                return new UsernameNotFoundException("Không tìm thấy tài khoản với email: " + email);
            });
        
        log.info("User found: {}, enabled: {}, role: {}", user.getEmail(), user.getIsEnabled(), user.getRole());
        
        // Check if account is enabled
        if (!Boolean.TRUE.equals(user.getIsEnabled())) {
            log.warn("User account is disabled: {}", email);
            throw new UsernameNotFoundException("Tài khoản đã bị khóa");
        }
        
        return new org.springframework.security.core.userdetails.User(
            user.getEmail(),
            user.getPassword(),
            user.getIsEnabled(),                  // enabled
            true,                                 // accountNonExpired
            true,                                 // credentialsNonExpired
            !Boolean.TRUE.equals(user.getIsBanned()), // accountNonLocked (use banned flag)
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}