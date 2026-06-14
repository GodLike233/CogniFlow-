package com.cogniflow.controller;

import com.cogniflow.entity.Course;
import com.cogniflow.service.CourseService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public List<Course> list() {
        return courseService.findAll();
    }

    @GetMapping("/{id}")
    public Course get(@PathVariable Long id) {
        return courseService.findById(id);
    }

    @PostMapping
    public Course create(@RequestBody Course course) {
        return courseService.save(course);
    }

    @PutMapping("/{id}")
    public Course update(@PathVariable Long id, @RequestBody Course course) {
        return courseService.update(id, course);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        courseService.delete(id);
        return "ok";
    }

    @GetMapping("/search")
    public List<Course> search(@RequestParam String keyword) {
        return courseService.search(keyword);
    }
}
