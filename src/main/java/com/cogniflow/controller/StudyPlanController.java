package com.cogniflow.controller;

import com.cogniflow.entity.StudyPlan;
import com.cogniflow.service.StudyPlanService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/plans")
public class StudyPlanController {

    private final StudyPlanService planService;

    public StudyPlanController(StudyPlanService planService) {
        this.planService = planService;
    }

    @PostMapping
    public StudyPlan create(@RequestBody Map<String, Object> body) {
        Long userId = toLong(body.get("userId"));
        String title = (String) body.get("title");
        String planType = (String) body.get("planType");
        String content = (String) body.get("content");
        Integer totalDays = body.get("totalDays") != null ? ((Number) body.get("totalDays")).intValue() : 0;
        return planService.save(userId, title, planType, content, totalDays);
    }

    @GetMapping("/user/{userId}")
    public List<StudyPlan> userPlans(@PathVariable Long userId) {
        return planService.findByUser(userId);
    }

    @PutMapping("/{id}/progress")
    public StudyPlan updateProgress(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Integer progressDays = body.get("progressDays") != null ? ((Number) body.get("progressDays")).intValue() : 0;
        return planService.updateProgress(id, progressDays);
    }

    @PutMapping("/{id}/content")
    public StudyPlan updateContent(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String content = (String) body.get("content");
        Integer totalDays = body.get("totalDays") != null ? ((Number) body.get("totalDays")).intValue() : null;
        return planService.updateContent(id, content, totalDays);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        planService.delete(id);
        return "ok";
    }

    private Long toLong(Object val) {
        if (val instanceof Number n) return n.longValue();
        return Long.parseLong(val.toString());
    }
}
