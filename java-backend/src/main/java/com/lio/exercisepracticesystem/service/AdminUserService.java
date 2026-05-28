package com.lio.exercisepracticesystem.service;

import com.lio.exercisepracticesystem.dto.AdminUserCreateRequest;
import com.lio.exercisepracticesystem.dto.AdminUserUpdateRequest;
import com.lio.exercisepracticesystem.dto.PagedUsersResponse;
import com.lio.exercisepracticesystem.dto.UserResponse;
import com.lio.exercisepracticesystem.entity.User;
import com.lio.exercisepracticesystem.entity.UserRole;
import com.lio.exercisepracticesystem.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public PagedUsersResponse list(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)));
        Page<User> result = StringUtils.hasText(keyword)
                ? userRepository.findByUsernameContainingIgnoreCase(keyword.trim(), pageable)
                : userRepository.findAll(pageable);
        return new PagedUsersResponse(
                result.getContent().stream().map(this::toResponse).toList(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.getNumber(),
                result.getSize()
        );
    }

    public UserResponse getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        return toResponse(user);
    }

    @Transactional
    public UserResponse create(AdminUserCreateRequest req) {
        if (!StringUtils.hasText(req.getUsername()) || !StringUtils.hasText(req.getPassword())) {
            throw new IllegalArgumentException("用户名和密码不能为空");
        }
        if (userRepository.existsByUsername(req.getUsername().trim())) {
            throw new IllegalArgumentException("用户名已存在");
        }
        UserRole role = parseRole(req.getRole(), UserRole.USER);

        User user = new User();
        user.setUsername(req.getUsername().trim());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setRole(role);
        user.setCreatedAt(java.time.LocalDateTime.now());
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse update(Long id, AdminUserUpdateRequest req, Long currentUserId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        if (StringUtils.hasText(req.getUsername()) && !req.getUsername().trim().equals(user.getUsername())) {
            if (userRepository.existsByUsername(req.getUsername().trim())) {
                throw new IllegalArgumentException("用户名已被占用");
            }
            user.setUsername(req.getUsername().trim());
        }
        if (StringUtils.hasText(req.getPassword())) {
            user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        }
        if (req.getRole() != null && StringUtils.hasText(req.getRole())) {
            UserRole newRole = parseRole(req.getRole(), user.getRole());
            ensureNotRemoveLastAdmin(user, newRole, currentUserId);
            user.setRole(newRole);
        }

        return toResponse(userRepository.save(user));
    }

    private void ensureNotRemoveLastAdmin(User target, UserRole newRole, Long currentUserId) {
        if (target.getRole() != UserRole.ADMIN || newRole == UserRole.ADMIN) {
            return;
        }
        long adminCount = userRepository.countByRole(UserRole.ADMIN);
        if (adminCount <= 1) {
            throw new IllegalArgumentException("系统至少需要保留一名管理员");
        }
    }

    @Transactional
    public void delete(Long id, Long currentUserId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        if (user.getId().equals(currentUserId)) {
            throw new IllegalArgumentException("不能删除当前登录账号");
        }
        if (user.getRole() == UserRole.ADMIN && userRepository.countByRole(UserRole.ADMIN) <= 1) {
            throw new IllegalArgumentException("不能删除唯一的管理员账号");
        }
        userRepository.delete(user);
    }

    private UserRole parseRole(String raw, UserRole defaultRole) {
        if (!StringUtils.hasText(raw)) {
            return defaultRole;
        }
        try {
            return UserRole.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("角色只能是 USER 或 ADMIN");
        }
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getCreatedAt(),
                user.getRole().name()
        );
    }
}
