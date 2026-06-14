package com.cogniflow.service;

import com.cogniflow.entity.KnowledgePoint;
import com.cogniflow.exception.BusinessException;
import com.cogniflow.repository.CourseRepository;
import com.cogniflow.repository.KnowledgePointRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class KnowledgePointService {

    private static final Logger log = LoggerFactory.getLogger(KnowledgePointService.class);
    private final KnowledgePointRepository kpRepository;
    private final CourseRepository courseRepository;

    public KnowledgePointService(KnowledgePointRepository kpRepository, CourseRepository courseRepository) {
        this.kpRepository = kpRepository;
        this.courseRepository = courseRepository;
    }

    public List<KnowledgePoint> findByCourse(Long courseId) {
        return kpRepository.findByCourseIdOrderByOrderIndexAsc(courseId);
    }

    public KnowledgePoint findById(Long id) {
        return kpRepository.findById(id)
                .orElseThrow(() -> new BusinessException("知识点不存在"));
    }

    public KnowledgePoint save(Long courseId, KnowledgePoint kp) {
        kp.setCourse(courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException("课程不存在")));
        log.info("新增知识点: {}", kp.getName());
        return kpRepository.save(kp);
    }

    public KnowledgePoint update(Long id, KnowledgePoint kp) {
        KnowledgePoint existing = findById(id);
        existing.setName(kp.getName());
        existing.setDescription(kp.getDescription());
        existing.setDifficulty(kp.getDifficulty());
        existing.setOrderIndex(kp.getOrderIndex());
        log.info("修改知识点: {}", id);
        return kpRepository.save(existing);
    }

    public void delete(Long id) {
        if (!kpRepository.existsById(id)) {
            throw new BusinessException("知识点不存在");
        }
        log.info("删除知识点: {}", id);
        kpRepository.deleteById(id);
    }

    public List<Map<String, Object>> buildKnowledgeTree(Long courseId) {
        List<KnowledgePoint> allPoints = kpRepository.findByCourseIdOrderByOrderIndexAsc(courseId);
        Map<Long, List<KnowledgePoint>> parentMap = new HashMap<>();
        List<KnowledgePoint> roots = new ArrayList<>();
        for (KnowledgePoint kp : allPoints) {
            if (kp.getParentId() == null) {
                roots.add(kp);
            } else {
                parentMap.computeIfAbsent(kp.getParentId(), k -> new ArrayList<>()).add(kp);
            }
        }
        List<Map<String, Object>> tree = new ArrayList<>();
        for (KnowledgePoint root : roots) {
            tree.add(buildNode(root, parentMap));
        }
        return tree;
    }

    private Map<String, Object> buildNode(KnowledgePoint kp, Map<Long, List<KnowledgePoint>> parentMap) {
        Map<String, Object> node = new LinkedHashMap<>();
        node.put("id", kp.getId());
        node.put("name", kp.getName());
        node.put("description", kp.getDescription());
        node.put("difficulty", kp.getDifficulty());
        List<KnowledgePoint> children = parentMap.getOrDefault(kp.getId(), new ArrayList<>());
        List<Map<String, Object>> childNodes = new ArrayList<>();
        for (KnowledgePoint child : children) {
            childNodes.add(buildNode(child, parentMap));
        }
        node.put("children", childNodes);
        return node;
    }
}
