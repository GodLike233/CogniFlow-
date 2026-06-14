package com.cogniflow.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "exam_answers")
public class ExamAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "exam_record_id", nullable = false)
    private Long examRecordId;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(name = "user_answer", length = 255)
    private String userAnswer;

    @Column(name = "is_correct")
    private Boolean isCorrect;

    private Integer score;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isCorrect == null) isCorrect = false;
        if (score == null) score = 0;
    }

    public ExamAnswer() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getExamRecordId() { return examRecordId; }
    public void setExamRecordId(Long examRecordId) { this.examRecordId = examRecordId; }
    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public String getUserAnswer() { return userAnswer; }
    public void setUserAnswer(String userAnswer) { this.userAnswer = userAnswer; }
    public Boolean getIsCorrect() { return isCorrect; }
    public void setIsCorrect(Boolean isCorrect) { this.isCorrect = isCorrect; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}