package com.cogniflow.repository;

import com.cogniflow.entity.ExamAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamAnswerRepository extends JpaRepository<ExamAnswer, Long> {
    List<ExamAnswer> findByExamRecordId(Long examRecordId);
    void deleteByExamRecordId(Long examRecordId);
    long countByExamRecordId(Long examRecordId);
}