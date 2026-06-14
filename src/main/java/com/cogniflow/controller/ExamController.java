package com.cogniflow.controller;

import com.cogniflow.entity.ExamAnswer;
import com.cogniflow.entity.ExamRecord;
import com.cogniflow.entity.Question;
import com.cogniflow.repository.ExamAnswerRepository;
import com.cogniflow.repository.ExamRecordRepository;
import com.cogniflow.repository.QuestionRepository;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/exam")
public class ExamController {

    private final QuestionRepository questionRepo;
    private final ExamRecordRepository examRecordRepo;
    private final ExamAnswerRepository examAnswerRepo;

    public ExamController(QuestionRepository questionRepo, ExamRecordRepository examRecordRepo,
                          ExamAnswerRepository examAnswerRepo) {
        this.questionRepo = questionRepo;
        this.examRecordRepo = examRecordRepo;
        this.examAnswerRepo = examAnswerRepo;
    }

    @PostMapping("/start")
    public Map<String, Object> start(@RequestBody Map<String, Object> body) {
        Long userId = toLong(body.get("userId"));
        Long courseId = toLong(body.get("courseId"));

        Optional<ExamRecord> existing = examRecordRepo.findByUserIdAndCourseIdAndStatus(userId, courseId, "IN_PROGRESS");
        if (existing.isPresent()) {
            ExamRecord record = existing.get();
            List<Question> questions = findQuestionsByIds(record.getQuestionIds());
            Map<String, Object> result = buildExamResponse(record, questions);
            result.put("resumed", true);
            return result;
        }

        List<Question> pool = questionRepo.findByCourseIdOrderByIdAsc(courseId);
        Collections.shuffle(pool, new Random());
        int count = Math.min(10, pool.size());
        List<Question> selected = pool.subList(0, count);
        String qIds = selected.stream().map(q -> q.getId().toString()).collect(Collectors.joining(","));

        ExamRecord record = new ExamRecord();
        record.setUserId(userId);
        record.setCourseId(courseId);
        record.setQuestionCount(selected.size());
        record.setTotalScore(selected.stream().mapToInt(q -> q.getScore() != null ? q.getScore() : 5).sum());
        record.setStatus("IN_PROGRESS");
        record.setStartTime(LocalDateTime.now());
        record.setQuestionIds(qIds);
        record = examRecordRepo.save(record);

        Map<String, Object> result = buildExamResponse(record, selected);
        result.put("resumed", false);
        return result;
    }

    @GetMapping("/in-progress/{userId}")
    public List<Map<String, Object>> inProgress(@PathVariable Long userId) {
        return examRecordRepo.findByUserIdAndStatusOrderByStartTimeDesc(userId, "IN_PROGRESS").stream().map(e -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", e.getId());
            m.put("courseId", e.getCourseId());
            m.put("questionCount", e.getQuestionCount());
            m.put("startTime", e.getStartTime() != null ? e.getStartTime().toString() : null);
            return m;
        }).toList();
    }

    @GetMapping("/resume/{examRecordId}")
    public Map<String, Object> resume(@PathVariable Long examRecordId) {
        ExamRecord record = examRecordRepo.findById(examRecordId).orElseThrow();
        if (!"IN_PROGRESS".equals(record.getStatus())) {
            throw new RuntimeException("考试已结束");
        }
        List<Question> questions = findQuestionsByIds(record.getQuestionIds());
        return buildExamResponse(record, questions);
    }

    @PostMapping("/abandon/{examRecordId}")
    public Map<String, Object> abandon(@PathVariable Long examRecordId) {
        ExamRecord record = examRecordRepo.findById(examRecordId).orElseThrow();
        if (!"IN_PROGRESS".equals(record.getStatus())) {
            throw new RuntimeException("考试已结束");
        }
        record.setStatus("ABANDONED");
        examRecordRepo.save(record);
        return Map.of("message", "已放弃");
    }

    @PostMapping("/submit")
    public Map<String, Object> submit(@RequestBody Map<String, Object> body) {
        Long examRecordId = toLong(body.get("examRecordId"));
        @SuppressWarnings("unchecked")
        List<Map<String, String>> answers = (List<Map<String, String>>) body.get("answers");

        ExamRecord record = examRecordRepo.findById(examRecordId).orElseThrow();
        if (!"IN_PROGRESS".equals(record.getStatus())) {
            throw new RuntimeException("考试已完成");
        }

        int correct = 0;
        int userScore = 0;
        List<Map<String, Object>> details = new ArrayList<>();

        for (Map<String, String> ans : answers) {
            Long qid = Long.parseLong(ans.get("questionId"));
            String userAnswer = ans.get("answer");
            Question q = questionRepo.findById(qid).orElse(null);
            if (q == null) continue;

            boolean isCorrect = normalizeAnswer(userAnswer).equalsIgnoreCase(normalizeAnswer(q.getAnswer()));
            int qScore = isCorrect ? (q.getScore() != null ? q.getScore() : 5) : 0;
            if (isCorrect) {
                correct++;
                userScore += qScore;
            }

            ExamAnswer ea = new ExamAnswer();
            ea.setExamRecordId(examRecordId);
            ea.setQuestionId(qid);
            ea.setUserAnswer(userAnswer);
            ea.setIsCorrect(isCorrect);
            ea.setScore(qScore);
            examAnswerRepo.save(ea);

            Map<String, Object> detail = new LinkedHashMap<>();
            detail.put("questionId", qid);
            detail.put("title", q.getTitle());
            detail.put("userAnswer", userAnswer);
            detail.put("correctAnswer", q.getAnswer());
            detail.put("correct", isCorrect);
            detail.put("score", qScore);
            detail.put("analysis", q.getAnalysis());
            if ("SINGLE".equals(q.getType()) || "MULTI".equals(q.getType())) {
                detail.put("optionA", q.getOptionA());
                detail.put("optionB", q.getOptionB());
                detail.put("optionC", q.getOptionC());
                detail.put("optionD", q.getOptionD());
            }
            details.add(detail);
        }

        record.setUserScore(userScore);
        record.setCorrectCount(correct);
        record.setStatus("COMPLETED");
        record.setEndTime(LocalDateTime.now());
        examRecordRepo.save(record);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("examRecordId", record.getId());
        result.put("totalScore", record.getTotalScore());
        result.put("userScore", userScore);
        result.put("correctCount", correct);
        result.put("questionCount", details.size());
        result.put("percent", record.getTotalScore() > 0 ? Math.round(userScore * 100.0 / record.getTotalScore()) : 0);
        result.put("details", details);
        return result;
    }

    @GetMapping("/answers/{examRecordId}")
    public List<Map<String, Object>> examAnswers(@PathVariable Long examRecordId) {
        return examAnswerRepo.findByExamRecordId(examRecordId).stream().map(ea -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", ea.getId());
            m.put("questionId", ea.getQuestionId());
            questionRepo.findById(ea.getQuestionId()).ifPresent(q -> {
                m.put("questionTitle", q.getTitle());
                m.put("correctAnswer", q.getAnswer());
                m.put("type", q.getType());
                m.put("analysis", q.getAnalysis());
            });
            m.put("userAnswer", ea.getUserAnswer());
            m.put("isCorrect", ea.getIsCorrect());
            m.put("score", ea.getScore());
            return m;
        }).toList();
    }

    @GetMapping("/history/{userId}")
    public List<Map<String, Object>> history(@PathVariable Long userId) {
        return examRecordRepo.findByUserIdOrderByStartTimeDesc(userId).stream().map(e -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", e.getId());
            m.put("courseId", e.getCourseId());
            m.put("totalScore", e.getTotalScore());
            m.put("userScore", e.getUserScore());
            m.put("correctCount", e.getCorrectCount());
            m.put("questionCount", e.getQuestionCount());
            m.put("status", e.getStatus());
            m.put("startTime", e.getStartTime() != null ? e.getStartTime().toString() : null);
            m.put("endTime", e.getEndTime() != null ? e.getEndTime().toString() : null);
            return m;
        }).toList();
    }

    private Map<String, Object> buildExamResponse(ExamRecord record, List<Question> questions) {
        List<Map<String, Object>> qList = questions.stream().map(q -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", q.getId());
            m.put("type", q.getType());
            m.put("title", q.getTitle());
            m.put("score", q.getScore());
            m.put("difficulty", q.getDifficulty());
            if ("SINGLE".equals(q.getType()) || "MULTI".equals(q.getType())) {
                m.put("optionA", q.getOptionA());
                m.put("optionB", q.getOptionB());
                m.put("optionC", q.getOptionC());
                m.put("optionD", q.getOptionD());
            }
            return m;
        }).toList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("examRecordId", record.getId());
        result.put("questions", qList);
        result.put("totalScore", record.getTotalScore());
        result.put("questionCount", questions.size());
        return result;
    }

    private List<Question> findQuestionsByIds(String ids) {
        if (ids == null || ids.isBlank()) return List.of();
        return Arrays.stream(ids.split(","))
                .map(String::trim)
                .map(Long::parseLong)
                .map(questionRepo::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private String normalizeAnswer(String s) {
        if (s == null) return "";
        s = s.trim().toUpperCase();
        if (s.equals("T") || s.equals("TRUE")) return "T";
        if (s.equals("F") || s.equals("FALSE")) return "F";
        char[] chars = s.replace(",", "").replace(" ", "").toCharArray();
        Arrays.sort(chars);
        return new String(chars);
    }

    private Long toLong(Object val) {
        if (val instanceof Number n) return n.longValue();
        return Long.parseLong(val.toString());
    }
}