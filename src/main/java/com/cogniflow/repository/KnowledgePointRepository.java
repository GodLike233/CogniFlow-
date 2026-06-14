package com.cogniflow.repository;

import com.cogniflow.entity.KnowledgePoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KnowledgePointRepository extends JpaRepository<KnowledgePoint, Long> {
    List<KnowledgePoint> findByCourseId(Long courseId);
    List<KnowledgePoint> findByParentId(Long parentId);
    List<KnowledgePoint> findByCourseIdAndParentIdIsNull(Long courseId);
    List<KnowledgePoint> findByCourseIdOrderByOrderIndexAsc(Long courseId);
    List<KnowledgePoint> findByDifficulty(String difficulty);
}
