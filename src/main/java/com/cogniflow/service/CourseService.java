package com.cogniflow.service;

import com.cogniflow.entity.Course;
import com.cogniflow.exception.BusinessException;
import com.cogniflow.repository.CourseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseService {

    private static final Logger log = LoggerFactory.getLogger(CourseService.class);
    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public List<Course> findAll() {
        log.info("查询所有课程");
        return courseRepository.findAll();
    }

    public Course findById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new BusinessException("课程不存在: id=" + id));
    }

    public Course save(Course course) {
        log.info("新增课程: {}", course.getName());
        return courseRepository.save(course);
    }

    public Course update(Long id, Course course) {
        Course existing = findById(id);
        existing.setName(course.getName());
        existing.setDescription(course.getDescription());
        existing.setCategory(course.getCategory());
        log.info("修改课程: {}", id);
        return courseRepository.save(existing);
    }

    public void delete(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new BusinessException("课程不存在: id=" + id);
        }
        log.info("删除课程 (关联知识点/考试记录保留，显示为已删除): {}", id);
        courseRepository.deleteById(id);
    }

    public List<Course> search(String keyword) {
        return courseRepository.findByNameContaining(keyword);
    }

    public List<Course> findByCategory(String category) {
        return courseRepository.findByCategory(category);
    }
}
