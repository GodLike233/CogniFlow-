package com.cogniflow.service;

import com.cogniflow.entity.User;
import com.cogniflow.exception.BusinessException;
import com.cogniflow.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户名或密码错误"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        log.info("用户登录: {} ({})", username, user.getRole());
        return user;
    }

    public User register(String username, String password, String role) {
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException("用户名已存在");
        }
        User user = "ADMIN".equals(role) ? new com.cogniflow.entity.Admin(username, passwordEncoder.encode(password))
                : new com.cogniflow.entity.Student(username, passwordEncoder.encode(password));
        log.info("注册新用户: {} (角色: {})", username, user.getRoleDisplayName());
        return userRepository.save(user);
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }
}
