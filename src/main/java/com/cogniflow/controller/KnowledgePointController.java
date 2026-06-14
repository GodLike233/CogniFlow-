package com.cogniflow.controller;

import com.cogniflow.entity.KnowledgePoint;
import com.cogniflow.service.KnowledgePointService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/knowledge-points")
public class KnowledgePointController {

    private final KnowledgePointService kpService;

    public KnowledgePointController(KnowledgePointService kpService) {
        this.kpService = kpService;
    }

    @GetMapping("/course/{courseId}")
    public List<KnowledgePoint> listByCourse(@PathVariable Long courseId) {
        return kpService.findByCourse(courseId);
    }

    @GetMapping("/{id}")
    public KnowledgePoint get(@PathVariable Long id) {
        return kpService.findById(id);
    }

    @PostMapping("/course/{courseId}")
    public KnowledgePoint create(@PathVariable Long courseId, @RequestBody KnowledgePoint kp) {
        return kpService.save(courseId, kp);
    }

    @PutMapping("/{id}")
    public KnowledgePoint update(@PathVariable Long id, @RequestBody KnowledgePoint kp) {
        return kpService.update(id, kp);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        kpService.delete(id);
        return "ok";
    }

    @GetMapping("/tree/{courseId}")
    public List<Map<String, Object>> tree(@PathVariable Long courseId) {
        return kpService.buildKnowledgeTree(courseId);
    }
}
