package com.lio.exercisepracticesystem.controller;

import com.lio.exercisepracticesystem.dto.AdminUserCreateRequest;
import com.lio.exercisepracticesystem.dto.AdminUserUpdateRequest;
import com.lio.exercisepracticesystem.dto.PagedUsersResponse;
import com.lio.exercisepracticesystem.dto.UserResponse;
import com.lio.exercisepracticesystem.entity.User;
import com.lio.exercisepracticesystem.repository.UserRepository;
import com.lio.exercisepracticesystem.service.AdminUserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;
    private final UserRepository userRepository;

    public AdminUserController(AdminUserService adminUserService, UserRepository userRepository) {
        this.adminUserService = adminUserService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public PagedUsersResponse list(
            @RequestParam(value = "keyword", defaultValue = "") String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        return adminUserService.list(keyword, page, size);
    }

    @GetMapping("/{id}")
    public UserResponse get(@PathVariable("id") Long id) {
        return adminUserService.getById(id);
    }

    @PostMapping
    public UserResponse create(@RequestBody AdminUserCreateRequest request) {
        return adminUserService.create(request);
    }

    @PutMapping("/{id}")
    public UserResponse update(
            @PathVariable("id") Long id,
            @RequestBody AdminUserUpdateRequest request,
            Authentication authentication
    ) {
        Long currentId = requireUserId(authentication.getName());
        return adminUserService.update(id, request, currentId);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") Long id, Authentication authentication) {
        Long currentId = requireUserId(authentication.getName());
        adminUserService.delete(id, currentId);
    }

    private Long requireUserId(String username) {
        User u = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        return u.getId();
    }
}
