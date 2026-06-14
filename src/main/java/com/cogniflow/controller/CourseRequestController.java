package com.cogniflow.controller;

import com.cogniflow.entity.*;
import com.cogniflow.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import com.volcengine.ark.runtime.service.ArkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/course-request")
public class CourseRequestController {

    private static final Logger log = LoggerFactory.getLogger(CourseRequestController.class);

    private final ArkService arkService;
    private final CourseRequestRepository requestRepo;
    private final CourseRepository courseRepo;
    private final KnowledgePointRepository kpRepo;
    private final QuestionRepository questionRepo;
    private final UserRepository userRepo;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${ark.model.id}")
    private String modelId;

    public CourseRequestController(ArkService arkService, CourseRequestRepository requestRepo,
                                   CourseRepository courseRepo, KnowledgePointRepository kpRepo,
                                   QuestionRepository questionRepo, UserRepository userRepo) {
        this.arkService = arkService;
        this.requestRepo = requestRepo;
        this.courseRepo = courseRepo;
        this.kpRepo = kpRepo;
        this.questionRepo = questionRepo;
        this.userRepo = userRepo;
    }

    @PostMapping("/submit")
    public Map<String, Object> submit(@RequestBody Map<String, Object> body) {
        Long userId = toLong(body.get("userId"));
        String topic = (String) body.get("topic");
        String description = (String) body.get("description");
        CourseRequest req = new CourseRequest();
        req.setUserId(userId);
        req.setTopic(topic);
        req.setDescription(description);
        req.setStatus("PENDING");
        requestRepo.save(req);
        return Map.of("id", req.getId(), "message", "申请已提交");
    }

    @GetMapping("/pending-count")
    public Map<String, Long> pendingCount() {
        return Map.of("count", requestRepo.countByStatus("PENDING"));
    }

    @GetMapping("/all")
    public List<Map<String, Object>> all() {
        return requestRepo.findAllByOrderByCreatedAtDesc().stream().map(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", r.getId());
            m.put("userId", r.getUserId());
            m.put("topic", r.getTopic());
            m.put("description", r.getDescription());
            m.put("status", r.getStatus());
            m.put("createdAt", r.getCreatedAt() != null ? r.getCreatedAt().toString() : null);
            userRepo.findById(r.getUserId()).ifPresent(u -> m.put("userName", u.getUsername()));
            return m;
        }).toList();
    }

    @PostMapping("/approve/{id}")
    public Map<String, Object> approve(@PathVariable Long id) {
        CourseRequest req = requestRepo.findById(id).orElseThrow();
        if (!"PENDING".equals(req.getStatus())) {
            throw new RuntimeException("该申请已处理");
        }
        req.setStatus("APPROVED");
        requestRepo.save(req);

        try {
            Map<String, Object> result = generateCourseByAI(req.getTopic(), req.getDescription());
            return result;
        } catch (Exception e) {
            log.error("AI生成课程失败", e);
            req.setStatus("PENDING");
            requestRepo.save(req);
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("error", "AI生成失败: " + e.getMessage());
            return err;
        }
    }

    @PostMapping("/reject/{id}")
    public Map<String, Object> reject(@PathVariable Long id) {
        CourseRequest req = requestRepo.findById(id).orElseThrow();
        req.setStatus("REJECTED");
        requestRepo.save(req);
        return Map.of("id", req.getId(), "status", "REJECTED");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> generateCourseByAI(String topic, String description) throws Exception {
        String prompt = "你是一个课程设计专家。请为课程「" + topic + "」（" + (description != null ? description : "") + "）生成课程知识树和考试题库。\n\n" +
                "必须严格返回纯JSON，不要任何解释、不要markdown代码块标记、不要```json。\n\n" +
                "JSON结构如下：\n" +
                "{\n" +
                "  \"course\": {\"name\": \"" + topic + "\", \"category\": \"计算机科学\", \"description\": \"课程描述一句\"},\n" +
                "  \"knowledgePoints\": [\n" +
                "    {\"name\": \"知识点1\", \"difficulty\": \"BEGINNER\", \"orderIndex\": 0, \"parentIdx\": -1},\n" +
                "    {\"name\": \"知识点2\", \"difficulty\": \"BEGINNER\", \"orderIndex\": 1, \"parentIdx\": -1},\n" +
                "    {\"name\": \"子知识点2.1\", \"difficulty\": \"MEDIUM\", \"orderIndex\": 2, \"parentIdx\": 1},\n" +
                "    {\"name\": \"子知识点2.2\", \"difficulty\": \"MEDIUM\", \"orderIndex\": 3, \"parentIdx\": 1}\n" +
                "  ],\n" +
                "  \"questions\": [\n" +
                "    {\"type\": \"SINGLE\", \"title\": \"题目\", \"optionA\": \"选项A\", \"optionB\": \"选项B\", \"optionC\": \"选项C\", \"optionD\": \"选项D\", \"answer\": \"A\", \"analysis\": \"解析\", \"score\": 5, \"difficulty\": \"EASY\"},\n" +
                "    {\"type\": \"TF\", \"title\": \"判断题\", \"answer\": \"T\", \"analysis\": \"解析\", \"score\": 5, \"difficulty\": \"EASY\"}\n" +
                "  ]\n" +
                "}\n\n" +
                "重要规则：\n" +
                "1. knowledgePoints数组长度必须=8，生成恰好8个知识点\n" +
                "2. parentIdx是父节点在数组中的下标索引(从0开始)，顶级节点parentIdx填-1\n" +
                "3. 层级分布：前2个是顶级节点(parentIdx=-1)，然后2个子节点挂在下标0上(parentIdx=0)，2个子节点挂在下标1上(parentIdx=1)，最后2个是子子节点挂在子节点上(parentIdx=2或3)\n" +
                "4. difficulty取BEGINNER/MEDIUM/ADVANCED\n" +
                "5. questions生成12道，4道SINGLE单选+4道MULTI多选+4道TF判断，难度均匀分布EASY/MEDIUM/HARD\n" +
                "6. 所有字符串值中的双引号必须用反斜杠转义写为\\\"\n" +
                "7. 只返回纯JSON，不要任何其他内容";

        List<com.volcengine.ark.runtime.model.completion.chat.ChatMessage> messages = new ArrayList<>();
        messages.add(com.volcengine.ark.runtime.model.completion.chat.ChatMessage.builder()
                .role(ChatMessageRole.USER).content(prompt).build());

        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(modelId).messages(messages).build();

        StringBuilder sb = new StringBuilder();
        arkService.createChatCompletion(request)
                .getChoices().forEach(c -> sb.append(c.getMessage().getContent()));
        String response = sb.toString();
        log.info("AI返回长度: {}", response.length());

        String json = extractJSON(response);
        log.info("提取JSON前200字: {}", json.length() > 200 ? json.substring(0, 200) : json);

        Map<String, Object> root = objectMapper.readValue(json, Map.class);
        return parseAndSave(root);
    }

    private String extractJSON(String response) {
        String cleaned = response
                .replace("```json", "")
                .replace("```", "")
                .trim();
        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return cleaned.substring(start, end + 1);
        }
        return cleaned;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseAndSave(Map<String, Object> root) {
        Map<String, Object> result = new LinkedHashMap<>();

        Map<String, Object> courseData = (Map<String, Object>) root.get("course");
        Course course = new Course();
        course.setName((String) courseData.get("name"));
        course.setCategory((String) courseData.get("category"));
        course.setDescription((String) courseData.get("description"));
        course = courseRepo.save(course);
        result.put("courseId", course.getId());
        result.put("courseName", course.getName());

        List<Map<String, Object>> kps = (List<Map<String, Object>>) root.get("knowledgePoints");
        Map<Integer, Long> dbIdByIndex = new HashMap<>();
        Map<Integer, Integer> parentIdxMap = new HashMap<>();

        for (int i = 0; i < kps.size(); i++) {
            Map<String, Object> kpData = kps.get(i);
            KnowledgePoint kp = new KnowledgePoint();
            kp.setCourse(course);
            kp.setName(String.valueOf(kpData.get("name")));
            kp.setDifficulty(String.valueOf(kpData.get("difficulty")));
            kp.setOrderIndex(toInt(kpData.get("orderIndex"), i + 1));
            kp.setParentId(null);
            kp = kpRepo.save(kp);
            dbIdByIndex.put(i, kp.getId());

            Object pIdx = kpData.get("parentIdx");
            if (pIdx != null) {
                int pi = toInt(pIdx, -1);
                if (pi >= 0) parentIdxMap.put(i, pi);
            }
        }

        for (Map.Entry<Integer, Integer> e : parentIdxMap.entrySet()) {
            Long childDbId = dbIdByIndex.get(e.getKey());
            Long parentDbId = dbIdByIndex.get(e.getValue());
            if (childDbId != null && parentDbId != null) {
                KnowledgePoint kp = kpRepo.findById(childDbId).orElse(null);
                if (kp != null) {
                    kp.setParentId(parentDbId);
                    kpRepo.save(kp);
                }
            }
        }

        result.put("kpCount", kps.size());

        List<Map<String, Object>> questions = (List<Map<String, Object>>) root.get("questions");
        int qCount = 0;
        for (Map<String, Object> qData : questions) {
            Question q = new Question();
            q.setCourseId(course.getId());
            q.setType(String.valueOf(qData.get("type")));
            q.setTitle(String.valueOf(qData.get("title")));
            q.setOptionA(qData.get("optionA") != null ? String.valueOf(qData.get("optionA")) : null);
            q.setOptionB(qData.get("optionB") != null ? String.valueOf(qData.get("optionB")) : null);
            q.setOptionC(qData.get("optionC") != null ? String.valueOf(qData.get("optionC")) : null);
            q.setOptionD(qData.get("optionD") != null ? String.valueOf(qData.get("optionD")) : null);
            q.setAnswer(String.valueOf(qData.get("answer")));
            q.setAnalysis(qData.get("analysis") != null ? String.valueOf(qData.get("analysis")) : null);
            q.setScore(toInt(qData.get("score"), 5));
            q.setDifficulty(String.valueOf(qData.get("difficulty")));
            questionRepo.save(q);
            qCount++;
        }
        result.put("questionCount", qCount);
        return result;
    }

    private int toInt(Object val, int defaultVal) {
        if (val == null) return defaultVal;
        if (val instanceof Number n) return n.intValue();
        try { return Integer.parseInt(val.toString()); } catch (NumberFormatException e) { return defaultVal; }
    }

    private Long toLong(Object val) {
        if (val instanceof Number n) return n.longValue();
        return Long.parseLong(val.toString());
    }
}
