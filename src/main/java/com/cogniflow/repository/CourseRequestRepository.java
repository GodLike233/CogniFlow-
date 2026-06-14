package com.cogniflow.repository;

import com.cogniflow.entity.CourseRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRequestRepository extends JpaRepository<CourseRequest, Long> {
    List<CourseRequest> findAllByOrderByCreatedAtDesc();
    long countByStatus(String status);
    List<CourseRequest> findByUserIdOrderByCreatedAtDesc(Long userId);
}
