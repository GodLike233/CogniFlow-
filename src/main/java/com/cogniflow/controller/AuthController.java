package com.cogniflow.controller;

import com.cogniflow.entity.User;
import com.cogniflow.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body, HttpServletRequest request) {
        User user = userService.login(body.get("username"), body.get("password"));
        request.getSession().setAttribute("loginUser", user);
        return Map.of("code", 200, "message", "登录成功",
                "userId", user.getId(),
                "username", user.getUsername(),
                "role", user.getRole(),
                "roleDisplay", user.getRoleDisplayName());
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> body) {
        String role = body.getOrDefault("role", "STUDENT");
        User user = userService.register(body.get("username"), body.get("password"), role);
        return Map.of("code", 200, "message", "注册成功",
                "userId", user.getId(), "username", user.getUsername());
    }
}
