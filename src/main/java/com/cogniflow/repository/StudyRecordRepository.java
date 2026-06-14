package com.cogniflow.repository;

import com.cogniflow.entity.StudyRecord;
import com.cogniflow.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudyRecordRepository extends JpaRepository<StudyRecord, Long> {
    List<StudyRecord> findByUserId(Long userId);
    List<StudyRecord> findByUserIdAndKnowledgePointId(Long userId, Long knowledgePointId);
    List<StudyRecord> findByKnowledgePointCourseIdAndUserId(Long courseId, Long userId);
    List<StudyRecord> findByUserIdOrderByCreatedAtDesc(Long userId);
}
