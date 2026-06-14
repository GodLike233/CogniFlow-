package com.cogniflow.repository;

import com.cogniflow.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByCourseIdOrderByIdAsc(Long courseId);
    long countByCourseId(Long courseId);
    long countByCourseIdAndDifficulty(Long courseId, String difficulty);
}
