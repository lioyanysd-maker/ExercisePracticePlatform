package com.lio.exercisepracticesystem.service;

import com.lio.exercisepracticesystem.dto.LoginRequest;
import com.lio.exercisepracticesystem.dto.LoginResponse;
import com.lio.exercisepracticesystem.dto.RegisterRequest;
import com.lio.exercisepracticesystem.dto.UserResponse;
import com.lio.exercisepracticesystem.entity.User;
import com.lio.exercisepracticesystem.entity.UserRole;
import com.lio.exercisepracticesystem.repository.UserRepository;
import com.lio.exercisepracticesystem.security.JwtUtil;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;

@Service
public class AuthService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       @Lazy AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public LoginResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("用户名已存在");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setCreatedAt(java.time.LocalDateTime.now());
        user.setRole(UserRole.USER);

        User saved = userRepository.save(user);

        org.springframework.security.core.userdetails.User springUser =
                new org.springframework.security.core.userdetails.User(
                        saved.getUsername(),
                        saved.getPasswordHash(),
                        authoritiesFor(saved)
                );

        String token = jwtUtil.generateToken(springUser);
        UserResponse userResponse = toResponse(saved);
        return new LoginResponse(token, userResponse);
    }

    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));

        org.springframework.security.core.userdetails.User springUser =
                new org.springframework.security.core.userdetails.User(
                        user.getUsername(),
                        user.getPasswordHash(),
                        authoritiesFor(user)
                );

        String token = jwtUtil.generateToken(springUser);
        UserResponse userResponse = toResponse(user);
        return new LoginResponse(token, userResponse);
    }

    public UserResponse currentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));
        return toResponse(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPasswordHash(),
                authoritiesFor(user)
        );
    }

    private Collection<? extends GrantedAuthority> authoritiesFor(User user) {
        UserRole role = user.getRole() != null ? user.getRole() : UserRole.USER;
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    private UserResponse toResponse(User user) {
        UserRole role = user.getRole() != null ? user.getRole() : UserRole.USER;
        return new UserResponse(user.getId(), user.getUsername(), user.getCreatedAt(), role.name());
    }
}

