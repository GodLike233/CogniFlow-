package com.cogniflow.config;

import com.cogniflow.entity.Admin;
import com.cogniflow.entity.Student;
import com.cogniflow.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!userRepository.existsByUsername("admin")) {
            userRepository.save(new Admin("admin", passwordEncoder.encode("admin123")));
            log.info("创建默认管理员: admin");
        }
        if (!userRepository.existsByUsername("student")) {
            userRepository.save(new Student("student", passwordEncoder.encode("123456")));
            log.info("创建默认学生: student");
        }
    }
}