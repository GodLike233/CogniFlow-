package com.cogniflow.repository;

import com.cogniflow.entity.ExamRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExamRecordRepository extends JpaRepository<ExamRecord, Long> {
    List<ExamRecord> findByUserIdOrderByStartTimeDesc(Long userId);
    List<ExamRecord> findAllByOrderByStartTimeDesc();
    long countByUserId(Long userId);
    Optional<ExamRecord> findByUserIdAndCourseIdAndStatus(Long userId, Long courseId, String status);
    List<ExamRecord> findByUserIdAndStatusOrderByStartTimeDesc(Long userId, String status);
}
