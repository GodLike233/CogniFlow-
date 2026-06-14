package com.cogniflow.controller;

import com.cogniflow.service.PlanService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/plan")
public class PlanController {

    private final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    @PostMapping("/generate")
    public Map<String, String> generate(@RequestBody Map<String, String> body) {
        String topic = body.get("topic");
        String planType = body.getOrDefault("planType", "challenge21");
        String result = planService.generatePlan(topic, planType);
        return Map.of("plan", result);
    }
}
