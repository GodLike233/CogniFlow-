package com.cogniflow.service;

import com.cogniflow.entity.StudyPlan;
import com.cogniflow.exception.BusinessException;
import com.cogniflow.repository.StudyPlanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudyPlanService {

    private static final Logger log = LoggerFactory.getLogger(StudyPlanService.class);
    private final StudyPlanRepository planRepository;

    public StudyPlanService(StudyPlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    public StudyPlan save(Long userId, String title, String planType, String content, Integer totalDays) {
        if (planRepository.existsByUserIdAndTitle(userId, title)) {
            throw new BusinessException("已存在同名计划「" + title + "」");
        }
        StudyPlan plan = new StudyPlan(userId, title, planType, content, totalDays);
        log.info("保存学习计划: userId={}, title={}, type={}", userId, title, planType);
        return planRepository.save(plan);
    }

    public List<StudyPlan> findByUser(Long userId) {
        return planRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public StudyPlan findById(Long id) {
        return planRepository.findById(id)
                .orElseThrow(() -> new BusinessException("计划不存在"));
    }

    public StudyPlan updateProgress(Long id, Integer progressDays) {
        StudyPlan plan = findById(id);
        if (plan.getTotalDays() != null && progressDays > plan.getTotalDays()) {
            progressDays = plan.getTotalDays();
        }
        plan.setProgressDays(progressDays);
        return planRepository.save(plan);
    }

    public StudyPlan updateContent(Long id, String content, Integer totalDays) {
        StudyPlan plan = findById(id);
        plan.setContent(content);
        if (totalDays != null) {
            plan.setTotalDays(totalDays);
            if (plan.getProgressDays() != null && plan.getProgressDays() > totalDays) {
                plan.setProgressDays(totalDays);
            }
        }
        log.info("更新计划内容: id={}", id);
        return planRepository.save(plan);
    }

    public void delete(Long id) {
        planRepository.deleteById(id);
    }

    public boolean existsByUserAndTitle(Long userId, String title) {
        return planRepository.existsByUserIdAndTitle(userId, title);
    }
}
