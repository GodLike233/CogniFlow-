package com.cogniflow.controller;

import com.cogniflow.entity.*;
import com.cogniflow.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
public class AdminApiController {

    private static final Logger log = LoggerFactory.getLogger(AdminApiController.class);

    private final UserRepository userRepo;
    private final CourseRepository courseRepo;
    private final KnowledgePointRepository kpRepo;
    private final StudyRecordRepository recordRepo;
    private final StudyPlanRepository planRepo;
    private final QuestionRepository questionRepo;
    private final ExamRecordRepository examRecordRepo;
    private final AnnouncementRepository announcementRepo;
    private final SystemLogRepository systemLogRepo;
    private final ExamAnswerRepository examAnswerRepo;
    private final CourseRequestRepository courseRequestRepo;
    private final CourseRequestController courseRequestCtrl;
    private final PasswordEncoder passwordEncoder;

    public AdminApiController(UserRepository userRepo, CourseRepository courseRepo,
                              KnowledgePointRepository kpRepo, StudyRecordRepository recordRepo,
                              StudyPlanRepository planRepo, QuestionRepository questionRepo,
                              ExamRecordRepository examRecordRepo, AnnouncementRepository announcementRepo,
                              SystemLogRepository systemLogRepo, ExamAnswerRepository examAnswerRepo,
                              CourseRequestRepository courseRequestRepo, CourseRequestController courseRequestCtrl,
                              PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.courseRepo = courseRepo;
        this.kpRepo = kpRepo;
        this.recordRepo = recordRepo;
        this.planRepo = planRepo;
        this.questionRepo = questionRepo;
        this.examRecordRepo = examRecordRepo;
        this.announcementRepo = announcementRepo;
        this.systemLogRepo = systemLogRepo;
        this.examAnswerRepo = examAnswerRepo;
        this.courseRequestRepo = courseRequestRepo;
        this.courseRequestCtrl = courseRequestCtrl;
        this.passwordEncoder = passwordEncoder;
    }

    private void saveLog(String action, String target, String detail) {
        SystemLog sl = new SystemLog();
        sl.setAction(action);
        sl.setDetail("[" + target + "] " + detail);
        sl.setUserId(null);
        sl.setCreatedAt(LocalDateTime.now());
        systemLogRepo.save(sl);
        log.info("系统日志: {} [{}] {}", action, target, detail);
    }

    @GetMapping("/overview")
    public Map<String, Object> overview() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("totalCourses", courseRepo.count());
        m.put("totalKp", kpRepo.count());
        m.put("totalStudents", userRepo.findAll().stream().filter(u -> "STUDENT".equals(u.getRole())).count());
        m.put("totalAdmins", userRepo.findAll().stream().filter(u -> "ADMIN".equals(u.getRole())).count());
        m.put("totalRecords", recordRepo.count());
        m.put("totalPlans", planRepo.count());
        m.put("totalQuestions", questionRepo.count());
        m.put("totalExams", examRecordRepo.count());
        m.put("completedExams", examRecordRepo.findAll().stream().filter(e -> "COMPLETED".equals(e.getStatus())).count());
        double avg = recordRepo.findAll().stream().filter(r -> r.getScore() != null).mapToDouble(r -> r.getScore()).average().orElse(0);
        m.put("avgScore", Math.round(avg * 100.0) / 100.0);
        double examAvg = examRecordRepo.findAll().stream()
                .filter(e -> e.getTotalScore() != null && e.getTotalScore() > 0)
                .mapToDouble(e -> (e.getUserScore() != null ? e.getUserScore().doubleValue() / e.getTotalScore() : 0) * 100)
                .average().orElse(0);
        m.put("avgExamScore", Math.round(examAvg * 100.0) / 100.0);
        return m;
    }

    // ===== 课程管理 CRUD =====
    @PostMapping("/courses")
    public Course createCourse(@RequestBody Course course) {
        Course saved = courseRepo.save(course);
        saveLog("CREATE", "课程", "创建课程: " + saved.getName());
        return saved;
    }

    @PutMapping("/courses/{id}")
    public Course updateCourse(@PathVariable Long id, @RequestBody Course course) {
        Course existing = courseRepo.findById(id).orElseThrow();
        existing.setName(course.getName());
        existing.setCategory(course.getCategory());
        existing.setDescription(course.getDescription());
        Course saved = courseRepo.save(existing);
        saveLog("UPDATE", "课程", "更新课程: " + saved.getName());
        return saved;
    }

    @DeleteMapping("/courses/{id}")
    public Map<String, Object> deleteCourse(@PathVariable Long id) {
        Course c = courseRepo.findById(id).orElse(null);
        String name = c != null ? c.getName() : "ID=" + id;
        courseRepo.deleteById(id);
        saveLog("DELETE", "课程", "删除课程: " + name + " (关联数据保留)");
        return Map.of("message", "已删除");
    }

    // ===== 知识点管理 CRUD =====
    @PostMapping("/knowledge-points/course/{courseId}")
    public KnowledgePoint createKp(@PathVariable Long courseId, @RequestBody KnowledgePoint kp) {
        Course course = courseRepo.findById(courseId).orElseThrow();
        kp.setCourse(course);
        KnowledgePoint saved = kpRepo.save(kp);
        saveLog("CREATE", "知识点", "新增知识点: " + saved.getName());
        return saved;
    }

    @PutMapping("/knowledge-points/{id}")
    public KnowledgePoint updateKp(@PathVariable Long id, @RequestBody KnowledgePoint kp) {
        KnowledgePoint existing = kpRepo.findById(id).orElseThrow();
        existing.setName(kp.getName());
        existing.setDescription(kp.getDescription());
        existing.setDifficulty(kp.getDifficulty());
        existing.setOrderIndex(kp.getOrderIndex());
        existing.setParentId(kp.getParentId());
        KnowledgePoint saved = kpRepo.save(existing);
        saveLog("UPDATE", "知识点", "更新知识点: " + saved.getName());
        return saved;
    }

    @DeleteMapping("/knowledge-points/{id}")
    public Map<String, Object> deleteKp(@PathVariable Long id) {
        KnowledgePoint kp = kpRepo.findById(id).orElse(null);
        String name = kp != null ? kp.getName() : "ID=" + id;
        kpRepo.deleteById(id);
        saveLog("DELETE", "知识点", "删除知识点: " + name);
        return Map.of("message", "已删除");
    }

    // ===== 学生管理 CRUD =====
    @GetMapping("/students")
    public List<Map<String, Object>> students() {
        return userRepo.findAll().stream()
                .filter(u -> "STUDENT".equals(u.getRole()))
                .map(u -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", u.getId());
                    m.put("username", u.getUsername());
                    m.put("email", u.getEmail());
                    List<StudyPlan> plans = planRepo.findByUserIdOrderByCreatedAtDesc(u.getId());
                    m.put("planCount", plans.size());
                    m.put("examCount", examRecordRepo.countByUserId(u.getId()));
                    double avgExam = examRecordRepo.findByUserIdOrderByStartTimeDesc(u.getId()).stream()
                            .filter(e -> e.getTotalScore() != null && e.getTotalScore() > 0)
                            .mapToDouble(e -> (e.getUserScore() != null ? e.getUserScore().doubleValue() / e.getTotalScore() : 0) * 100)
                            .average().orElse(0);
                    m.put("avgExamScore", Math.round(avgExam * 10.0) / 10.0);
                    return m;
                }).toList();
    }

    @GetMapping("/student/{id}")
    public Map<String, Object> studentDetail(@PathVariable Long id) {
        Map<String, Object> m = new LinkedHashMap<>();
        User user = userRepo.findById(id).orElse(null);
        if (user == null) return m;
        m.put("id", user.getId());
        m.put("username", user.getUsername());
        m.put("email", user.getEmail());
        m.put("role", user.getRole());

        List<StudyPlan> plans = planRepo.findByUserIdOrderByCreatedAtDesc(user.getId());
        m.put("planCount", plans.size());
        m.put("activePlans", plans.stream().filter(p -> p.getProgressDays() != null && p.getTotalDays() != null && p.getProgressDays() < p.getTotalDays()).count());
        m.put("completedPlans", plans.stream().filter(p -> p.getProgressDays() != null && p.getTotalDays() != null && p.getProgressDays() >= p.getTotalDays()).count());

        List<ExamRecord> exams = examRecordRepo.findByUserIdOrderByStartTimeDesc(user.getId());
        m.put("examCount", exams.size());
        double avgExam = exams.stream()
                .filter(e -> e.getTotalScore() != null && e.getTotalScore() > 0)
                .mapToDouble(e -> (e.getUserScore() != null ? e.getUserScore().doubleValue() / e.getTotalScore() : 0) * 100)
                .average().orElse(0);
        m.put("avgExamScore", Math.round(avgExam * 10.0) / 10.0);

        List<StudyRecord> records = recordRepo.findByUserId(id);
        m.put("totalRecords", records.size());
        double avgStudy = records.stream().filter(r -> r.getScore() != null).mapToDouble(r -> r.getScore()).average().orElse(0);
        m.put("avgScore", Math.round(avgStudy * 10.0) / 10.0);
        long totalTime = records.stream().filter(r -> r.getTimeSpent() != null).mapToLong(r -> r.getTimeSpent()).sum();
        m.put("totalTimeSpent", totalTime);
        long passed = records.stream().filter(r -> "COMPLETED".equals(r.getStatus()) && (r.getScore() == null || r.getScore() >= 60)).count();
        long failed = records.stream().filter(r -> r.getScore() != null && r.getScore() < 60).count();
        long total = passed + failed;
        m.put("passedCount", passed);
        m.put("failedCount", failed);
        m.put("passRate", total > 0 ? Math.round(passed * 100.0 / total) : 0);
        return m;
    }

    @PostMapping("/students")
    public Map<String, Object> createStudent(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        if (userRepo.existsByUsername(username)) throw new RuntimeException("用户名已存在");
        Student student = new Student(username, passwordEncoder.encode(password));
        student.setEmail(body.get("email"));
        userRepo.save(student);
        saveLog("CREATE", "学生", "创建学生: " + username);
        return Map.of("id", student.getId(), "message", "创建成功");
    }

    @PutMapping("/students/{id}")
    public Map<String, Object> updateStudent(@PathVariable Long id, @RequestBody Map<String, String> body) {
        User user = userRepo.findById(id).orElseThrow(() -> new RuntimeException("用户不存在"));
        if (body.containsKey("email")) user.setEmail(body.get("email"));
        if (body.containsKey("password") && !body.get("password").isBlank()) user.setPassword(passwordEncoder.encode(body.get("password")));
        userRepo.save(user);
        saveLog("UPDATE", "学生", "更新学生: " + user.getUsername());
        return Map.of("message", "更新成功");
    }

    @DeleteMapping("/students/{id}")
    public Map<String, Object> deleteStudent(@PathVariable Long id) {
        User user = userRepo.findById(id).orElse(null);
        String name = user != null ? user.getUsername() : "ID=" + id;
        userRepo.deleteById(id);
        saveLog("DELETE", "学生", "删除学生: " + name);
        return Map.of("message", "已删除");
    }

    // ===== 考试记录 =====
    @GetMapping("/all-exams")
    public List<Map<String, Object>> allExams() {
        return examRecordRepo.findAllByOrderByStartTimeDesc().stream().map(e -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", e.getId());
            m.put("userId", e.getUserId());
            m.put("courseId", e.getCourseId());
            m.put("totalScore", e.getTotalScore());
            m.put("userScore", e.getUserScore());
            m.put("correctCount", e.getCorrectCount());
            m.put("questionCount", e.getQuestionCount());
            m.put("status", e.getStatus());
            m.put("startTime", e.getStartTime() != null ? e.getStartTime().toString() : null);
            m.put("endTime", e.getEndTime() != null ? e.getEndTime().toString() : null);
            userRepo.findById(e.getUserId()).ifPresent(u -> m.put("userName", u.getUsername()));
            if (e.getCourseId() != null) courseRepo.findById(e.getCourseId()).ifPresent(c -> m.put("courseName", c.getName()));
            else m.put("courseName", "(已删除课程)");
            return m;
        }).toList();
    }

    @DeleteMapping("/exams/{id}")
    public Map<String, Object> deleteExam(@PathVariable Long id) {
        ExamRecord er = examRecordRepo.findById(id).orElse(null);
        examAnswerRepo.deleteByExamRecordId(id);
        examRecordRepo.deleteById(id);
        saveLog("DELETE", "考试", "删除考试记录 ID=" + id + (er != null ? " (" + er.getUserId() + ")" : ""));
        return Map.of("message", "已删除");
    }

    // ===== 计划管理 CRUD =====
    @GetMapping("/all-plans")
    public List<Map<String, Object>> allPlans() {
        return planRepo.findAll().stream().map(p -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", p.getId());
            m.put("userId", p.getUserId());
            m.put("title", p.getTitle());
            m.put("planType", p.getPlanType());
            m.put("content", p.getContent());
            m.put("totalDays", p.getTotalDays());
            m.put("progressDays", p.getProgressDays());
            m.put("createdAt", p.getCreatedAt() != null ? p.getCreatedAt().toString() : null);
            return m;
        }).toList();
    }

    @PostMapping("/plans")
    public StudyPlan createPlan(@RequestBody StudyPlan plan) {
        StudyPlan saved = planRepo.save(plan);
        saveLog("CREATE", "计划", "创建计划: " + saved.getTitle());
        return saved;
    }

    @PutMapping("/plans/{id}")
    public StudyPlan updatePlan(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        StudyPlan plan = planRepo.findById(id).orElseThrow();
        if (body.containsKey("title")) plan.setTitle((String) body.get("title"));
        if (body.containsKey("planType")) plan.setPlanType((String) body.get("planType"));
        if (body.containsKey("content")) plan.setContent((String) body.get("content"));
        if (body.containsKey("totalDays")) plan.setTotalDays(((Number) body.get("totalDays")).intValue());
        if (body.containsKey("progressDays")) plan.setProgressDays(((Number) body.get("progressDays")).intValue());
        StudyPlan saved = planRepo.save(plan);
        saveLog("UPDATE", "计划", "更新计划: " + saved.getTitle());
        return saved;
    }

    @DeleteMapping("/plans/{id}")
    public Map<String, Object> deletePlan(@PathVariable Long id) {
        StudyPlan plan = planRepo.findById(id).orElse(null);
        String name = plan != null ? plan.getTitle() : "ID=" + id;
        planRepo.deleteById(id);
        saveLog("DELETE", "计划", "删除计划: " + name);
        return Map.of("message", "已删除");
    }

    // ===== 题库管理 =====
    @GetMapping("/questions/course/{courseId}")
    public List<Question> questionsByCourse(@PathVariable Long courseId) {
        return questionRepo.findByCourseIdOrderByIdAsc(courseId);
    }

    @GetMapping("/questions/stats/{courseId}")
    public Map<String, Object> questionStats(@PathVariable Long courseId) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("total", questionRepo.countByCourseId(courseId));
        m.put("easyCount", questionRepo.countByCourseIdAndDifficulty(courseId, "EASY"));
        m.put("mediumCount", questionRepo.countByCourseIdAndDifficulty(courseId, "MEDIUM"));
        m.put("hardCount", questionRepo.countByCourseIdAndDifficulty(courseId, "HARD"));
        return m;
    }

    @PostMapping("/questions")
    public Question createQuestion(@RequestBody Question q) {
        Question saved = questionRepo.save(q);
        saveLog("CREATE", "题目", "新增题目: " + truncate(saved.getTitle(), 50));
        return saved;
    }

    @PutMapping("/questions/{id}")
    public Question updateQuestion(@PathVariable Long id, @RequestBody Question q) {
        Question existing = questionRepo.findById(id).orElseThrow();
        existing.setCourseId(q.getCourseId());
        existing.setType(q.getType());
        existing.setTitle(q.getTitle());
        existing.setOptionA(q.getOptionA());
        existing.setOptionB(q.getOptionB());
        existing.setOptionC(q.getOptionC());
        existing.setOptionD(q.getOptionD());
        existing.setAnswer(q.getAnswer());
        existing.setAnalysis(q.getAnalysis());
        existing.setScore(q.getScore());
        existing.setDifficulty(q.getDifficulty());
        Question saved = questionRepo.save(existing);
        saveLog("UPDATE", "题目", "更新题目: " + truncate(saved.getTitle(), 50));
        return saved;
    }

    @DeleteMapping("/questions/{id}")
    public Map<String, Object> deleteQuestion(@PathVariable Long id) {
        Question q = questionRepo.findById(id).orElse(null);
        String name = q != null ? truncate(q.getTitle(), 50) : "ID=" + id;
        questionRepo.deleteById(id);
        saveLog("DELETE", "题目", "删除题目: " + name);
        return Map.of("message", "已删除");
    }

    // ===== 公告管理 CRUD =====
    @GetMapping("/announcements")
    public List<Announcement> announcements() {
        return announcementRepo.findAllByOrderByPinnedDescCreatedAtDesc();
    }

    @PostMapping("/announcements")
    public Announcement createAnnouncement(@RequestBody Announcement a) {
        Announcement saved = announcementRepo.save(a);
        saveLog("CREATE", "公告", "发布公告: " + saved.getTitle());
        return saved;
    }

    @PutMapping("/announcements/{id}")
    public Announcement updateAnnouncement(@PathVariable Long id, @RequestBody Announcement a) {
        Announcement existing = announcementRepo.findById(id).orElseThrow();
        existing.setTitle(a.getTitle());
        existing.setContent(a.getContent());
        existing.setPinned(a.getPinned());
        if (a.getPublisherId() != null) existing.setPublisherId(a.getPublisherId());
        Announcement saved = announcementRepo.save(existing);
        saveLog("UPDATE", "公告", "更新公告: " + saved.getTitle());
        return saved;
    }

    @DeleteMapping("/announcements/{id}")
    public Map<String, Object> deleteAnnouncement(@PathVariable Long id) {
        Announcement a = announcementRepo.findById(id).orElse(null);
        String name = a != null ? a.getTitle() : "ID=" + id;
        announcementRepo.deleteById(id);
        saveLog("DELETE", "公告", "删除公告: " + name);
        return Map.of("message", "已删除");
    }

    // ===== 系统日志 =====
    @GetMapping("/logs")
    public List<SystemLog> logs() {
        return systemLogRepo.findTop100ByOrderByCreatedAtDesc();
    }

    @DeleteMapping("/logs")
    public Map<String, Object> clearLogs() {
        systemLogRepo.deleteAll();
        saveLog("CLEAR", "日志", "清空系统日志");
        return Map.of("message", "日志已清空");
    }

    // ===== AI 生成课程 =====
    @PostMapping("/generate-course")
    public Map<String, Object> generateCourse(@RequestBody Map<String, String> body) {
        String topic = body.get("topic");
        if (topic == null || topic.isBlank()) throw new RuntimeException("请输入课程主题");
        Map<String, Object> reqBody = new LinkedHashMap<>();
        reqBody.put("userId", userRepo.findAll().stream().filter(u -> "ADMIN".equals(u.getRole())).findFirst().orElseThrow().getId());
        reqBody.put("topic", topic);
        reqBody.put("description", body.getOrDefault("description", ""));
        Map<String, Object> submitted = courseRequestCtrl.submit(reqBody);
        Long requestId = ((Number) submitted.get("id")).longValue();
        Map<String, Object> result = courseRequestCtrl.approve(requestId);
        saveLog("AI_GENERATE", "课程", "AI生成课程: " + topic);
        return result;
    }

    // ===== 导出 =====
    @GetMapping("/export/{type}")
    public List<?> export(@PathVariable String type) {
        return switch (type) {
            case "courses" -> courseRepo.findAll();
            case "students" -> userRepo.findAll().stream().filter(u -> "STUDENT".equals(u.getRole())).toList();
            case "exams" -> examRecordRepo.findAll();
            case "plans" -> planRepo.findAll();
            default -> List.of();
        };
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }
}