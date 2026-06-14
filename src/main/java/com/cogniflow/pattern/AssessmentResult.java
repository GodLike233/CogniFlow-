package com.cogniflow.pattern;

import java.util.List;

public class AssessmentResult {

    private final double totalScore;
    private final double averageScore;
    private final String level;

    public AssessmentResult(double totalScore, double averageScore, String level) {
        this.totalScore = totalScore;
        this.averageScore = averageScore;
        this.level = level;
    }

    public double getTotalScore() { return totalScore; }
    public double getAverageScore() { return averageScore; }
    public String getLevel() { return level; }
}
