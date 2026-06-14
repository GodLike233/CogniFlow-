package com.cogniflow.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "study_records")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class StudyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "knowledge_point_id")
    @JsonIgnore
    private KnowledgePoint knowledgePoint;

    @Column(length = 20)
    private String status;

    private Double score;

    @Column(name = "time_spent")
    private Integer timeSpent;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public StudyRecord() {}

    public StudyRecord(User user, KnowledgePoint knowledgePoint, String status, Double score, Integer timeSpent) {
        this.user = user;
        this.knowledgePoint = knowledgePoint;
        this.status = status;
        this.score = score;
        this.timeSpent = timeSpent;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public KnowledgePoint getKnowledgePoint() { return knowledgePoint; }
    public void setKnowledgePoint(KnowledgePoint knowledgePoint) { this.knowledgePoint = knowledgePoint; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }
    public Integer getTimeSpent() { return timeSpent; }
    public void setTimeSpent(Integer timeSpent) { this.timeSpent = timeSpent; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
