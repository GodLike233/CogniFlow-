package com.cogniflow.pattern;

import org.springframework.stereotype.Component;

@Component
public class AssessmentStrategyFactory {

    private final AverageBasedStrategy averageBasedStrategy;
    private final WeightedBasedStrategy weightedBasedStrategy;

    public AssessmentStrategyFactory(AverageBasedStrategy averageBasedStrategy, WeightedBasedStrategy weightedBasedStrategy) {
        this.averageBasedStrategy = averageBasedStrategy;
        this.weightedBasedStrategy = weightedBasedStrategy;
    }

    public AssessmentStrategy getStrategy(String type) {
        return switch (type) {
            case "weighted" -> weightedBasedStrategy;
            case "average" -> averageBasedStrategy;
            default -> averageBasedStrategy;
        };
    }
}
