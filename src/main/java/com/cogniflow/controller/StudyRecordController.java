package com.cogniflow.controller;

import com.cogniflow.entity.StudyRecord;
import com.cogniflow.pattern.AssessmentResult;
import com.cogniflow.service.StudyRecordService;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/study-records")
public class StudyRecordController {

    private final StudyRecordService recordService;

    public StudyRecordController(StudyRecordService recordService) {
        this.recordService = recordService;
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody Map<String, Object> body) {
        Long userId = toLong(body.get("userId"));
        Long kpId = toLong(body.get("kpId"));
        String status = (String) body.get("status");
        Double score = body.get("score") != null ? ((Number) body.get("score")).doubleValue() : null;
        Integer timeSpent = body.get("timeSpent") != null ? ((Number) body.get("timeSpent")).intValue() : null;
        StudyRecord record = recordService.save(userId, kpId, status, score, timeSpent);
        return toMap(record);
    }

    @GetMapping("/user/{userId}")
    public List<Map<String, Object>> userRecords(@PathVariable Long userId) {
        return recordService.findByUser(userId).stream().map(this::toMap).toList();
    }

    @GetMapping("/user/{userId}/course/{courseId}")
    public List<Map<String, Object>> userCourseRecords(@PathVariable Long userId, @PathVariable Long courseId) {
        return recordService.findByUserAndCourse(userId, courseId).stream().map(this::toMap).toList();
    }

    @GetMapping("/user/{userId}/assess")
    public AssessmentResult assess(@PathVariable Long userId, @RequestParam(defaultValue = "average") String strategy) {
        return recordService.assessByUser(userId, strategy);
    }

    @GetMapping("/user/{userId}/statistics")
    public Map<String, Object> statistics(@PathVariable Long userId) {
        return recordService.getStatistics(userId);
    }

    private Map<String, Object> toMap(StudyRecord r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", r.getId());
        m.put("status", r.getStatus());
        m.put("score", r.getScore());
        m.put("timeSpent", r.getTimeSpent());
        m.put("userId", r.getUser() != null ? r.getUser().getId() : null);
        m.put("kpId", r.getKnowledgePoint() != null ? r.getKnowledgePoint().getId() : null);
        m.put("kpName", r.getKnowledgePoint() != null ? r.getKnowledgePoint().getName() : null);
        m.put("createdAt", r.getCreatedAt() != null ? r.getCreatedAt().toString() : null);
        return m;
    }

    private Long toLong(Object val) {
        if (val instanceof Number n) return n.longValue();
        return Long.parseLong(val.toString());
    }
}
