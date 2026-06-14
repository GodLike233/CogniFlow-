package com.cogniflow.pattern;

import java.util.List;

public interface AssessmentStrategy {

    AssessmentResult assess(List<Double> records);

    String getStrategyName();
}
