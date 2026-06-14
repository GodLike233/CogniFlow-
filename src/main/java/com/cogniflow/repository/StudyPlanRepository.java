package com.cogniflow.repository;

import com.cogniflow.entity.StudyPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudyPlanRepository extends JpaRepository<StudyPlan, Long> {
    List<StudyPlan> findByUserIdOrderByCreatedAtDesc(Long userId);
    boolean existsByUserIdAndTitle(Long userId, String title);
}
