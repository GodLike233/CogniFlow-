package com.cogniflow.controller;

import com.cogniflow.service.CourseService;
import com.cogniflow.service.KnowledgePointService;
import com.cogniflow.service.StudyRecordService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final CourseService courseService;
    private final KnowledgePointService kpService;
    private final StudyRecordService recordService;

    public DashboardController(CourseService courseService, KnowledgePointService kpService, StudyRecordService recordService) {
        this.courseService = courseService;
        this.kpService = kpService;
        this.recordService = recordService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("courses", courseService.findAll());
        return "dashboard";
    }
}
