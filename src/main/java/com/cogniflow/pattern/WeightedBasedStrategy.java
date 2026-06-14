package com.cogniflow.pattern;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WeightedBasedStrategy implements AssessmentStrategy {

    @Override
    public AssessmentResult assess(List<Double> records) {
        if (records == null || records.isEmpty()) {
            return new AssessmentResult(0, 0, "未评估");
        }
        double total = 0;
        double weightSum = 0;
        for (int i = 0; i < records.size(); i++) {
            double weight = records.size() - i;
            total += records.get(i) * weight;
            weightSum += weight;
        }
        double avg = total / weightSum;
        String level;
        if (avg >= 90) level = "优秀";
        else if (avg >= 75) level = "良好";
        else if (avg >= 60) level = "及格";
        else level = "需加强";
        return new AssessmentResult(Math.round(total * 100.0) / 100.0, Math.round(avg * 100.0) / 100.0, level);
    }

    @Override
    public String getStrategyName() {
        return "加权评估策略";
    }
}
