package com.cogniflow.service;

import com.cogniflow.entity.KnowledgePoint;
import com.cogniflow.entity.StudyRecord;
import com.cogniflow.entity.User;
import com.cogniflow.exception.BusinessException;
import com.cogniflow.pattern.AssessmentResult;
import com.cogniflow.pattern.AssessmentStrategy;
import com.cogniflow.pattern.AssessmentStrategyFactory;
import com.cogniflow.repository.KnowledgePointRepository;
import com.cogniflow.repository.StudyRecordRepository;
import com.cogniflow.repository.UserRepository;
import com.cogniflow.util.CollectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class StudyRecordService {

    private static final Logger log = LoggerFactory.getLogger(StudyRecordService.class);
    private final StudyRecordRepository recordRepository;
    private final UserRepository userRepository;
    private final KnowledgePointRepository kpRepository;
    private final AssessmentStrategyFactory strategyFactory;

    public StudyRecordService(StudyRecordRepository recordRepository,
                              UserRepository userRepository,
                              KnowledgePointRepository kpRepository,
                              AssessmentStrategyFactory strategyFactory) {
        this.recordRepository = recordRepository;
        this.userRepository = userRepository;
        this.kpRepository = kpRepository;
        this.strategyFactory = strategyFactory;
    }

    public StudyRecord save(Long userId, Long kpId, String status, Double score, Integer timeSpent) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        KnowledgePoint kp = kpRepository.findById(kpId)
                .orElseThrow(() -> new BusinessException("知识点不存在"));
        StudyRecord record = new StudyRecord(user, kp, status, score, timeSpent);
        log.info("记录学习: 用户={}, 知识点={}, 状态={}", userId, kpId, status);
        return recordRepository.save(record);
    }

    public List<StudyRecord> findByUser(Long userId) {
        return recordRepository.findByUserId(userId);
    }

    public List<StudyRecord> findByUserAndCourse(Long userId, Long courseId) {
        return recordRepository.findByKnowledgePointCourseIdAndUserId(courseId, userId);
    }

    public AssessmentResult assessByUser(Long userId, String strategyType) {
        List<StudyRecord> records = findByUser(userId);
        List<Double> scores = records.stream()
                .filter(r -> r.getScore() != null)
                .map(StudyRecord::getScore)
                .collect(Collectors.toList());
        AssessmentStrategy strategy = strategyFactory.getStrategy(strategyType);
        return strategy.assess(scores);
    }

    public Map<String, Object> getStatistics(Long userId) {
        List<StudyRecord> records = findByUser(userId);
        double totalScore = 0;
        int count = 0;
        int passedCount = 0;
        int totalTime = 0;
        List<StudyRecord> failedList = new ArrayList<>();

        for (StudyRecord r : records) {
            if (r.getScore() != null) {
                totalScore += r.getScore();
                count++;
                if (r.getScore() >= 60) passedCount++;
            }
            if (r.getTimeSpent() != null) totalTime += r.getTimeSpent();
        }

        List<StudyRecord> failed = CollectionUtil.getFailedRecords(records);
        Map<String, List<StudyRecord>> grouped = CollectionUtil.groupByStatus(records);

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalRecords", records.size());
        stats.put("averageScore", count == 0 ? 0 : Math.round(totalScore / count * 100.0) / 100.0);
        stats.put("passRate", count == 0 ? 0 : Math.round((double) passedCount / count * 10000.0) / 100.0);
        stats.put("totalTimeSpent", totalTime);
        stats.put("failedCount", failed.size());
        stats.put("failedKpIds", failed.stream().map(r -> {
            Map<String, Object> m = new HashMap<>();
            m.put("recordId", r.getId());
            m.put("score", r.getScore());
            m.put("kpName", r.getKnowledgePoint() != null ? r.getKnowledgePoint().getName() : null);
            return m;
        }).toList());
        stats.put("statusCounts", grouped.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().size())));
        log.info("统计学习数据: 用户={}, 总记录={}, 均分={}", userId, records.size(),
                stats.get("averageScore"));
        return stats;
    }
}
