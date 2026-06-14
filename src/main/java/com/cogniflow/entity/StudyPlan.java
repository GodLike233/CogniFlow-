package com.cogniflow.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "study_plans")
public class StudyPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 50)
    private String planType;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "total_days")
    private Integer totalDays;

    @Column(name = "progress_days")
    private Integer progressDays;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (progressDays == null) progressDays = 0;
    }

    public StudyPlan() {}

    public StudyPlan(Long userId, String title, String planType, String content, Integer totalDays) {
        this.userId = userId;
        this.title = title;
        this.planType = planType;
        this.content = content;
        this.totalDays = totalDays;
        this.progressDays = 0;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getPlanType() { return planType; }
    public void setPlanType(String planType) { this.planType = planType; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getTotalDays() { return totalDays; }
    public void setTotalDays(Integer totalDays) { this.totalDays = totalDays; }
    public Integer getProgressDays() { return progressDays; }
    public void setProgressDays(Integer progressDays) { this.progressDays = progressDays; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
