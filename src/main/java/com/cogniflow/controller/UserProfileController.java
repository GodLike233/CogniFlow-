package com.cogniflow.controller;

import com.cogniflow.entity.User;
import com.cogniflow.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserProfileController {

    private final UserRepository userRepo;

    public UserProfileController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @GetMapping("/{id}")
    public Map<String, Object> getProfile(@PathVariable Long id) {
        User user = userRepo.findById(id).orElseThrow(() -> new RuntimeException("用户不存在"));
        return Map.of(
            "id", user.getId(),
            "username", user.getUsername(),
            "email", user.getEmail() != null ? user.getEmail() : "",
            "role", user.getRole()
        );
    }

    @PutMapping("/{id}")
    public Map<String, Object> updateProfile(@PathVariable Long id, @RequestBody Map<String, String> body) {
        User user = userRepo.findById(id).orElseThrow(() -> new RuntimeException("用户不存在"));
        if (body.containsKey("email")) user.setEmail(body.get("email"));
        if (body.containsKey("password") && !body.get("password").isBlank()) user.setPassword(body.get("password"));
        userRepo.save(user);
        return Map.of("message", "更新成功");
    }
}
