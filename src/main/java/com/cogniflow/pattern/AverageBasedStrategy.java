package com.cogniflow.pattern;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AverageBasedStrategy implements AssessmentStrategy {

    @Override
    public AssessmentResult assess(List<Double> records) {
        if (records == null || records.isEmpty()) {
            return new AssessmentResult(0, 0, "未评估");
        }
        double total = 0;
        for (Double r : records) {
            total += r;
        }
        double avg = total / records.size();
        String level;
        if (avg >= 90) level = "优秀";
        else if (avg >= 75) level = "良好";
        else if (avg >= 60) level = "及格";
        else level = "需加强";
        return new AssessmentResult(total, Math.round(avg * 100.0) / 100.0, level);
    }

    @Override
    public String getStrategyName() {
        return "平均分评估策略";
    }
}
