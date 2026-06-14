package com.cogniflow.service;

import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import com.volcengine.ark.runtime.service.ArkService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlanService {

    private final ArkService arkService;

    @Value("${ark.model.id}")
    private String modelId;

    public PlanService(ArkService arkService) {
        this.arkService = arkService;
    }

    public String generatePlan(String topic, String planType) {
        String prompt = switch (planType) {
            case "challenge21" -> build21DayPrompt(topic);
            case "speedrun" -> buildSpeedrunPrompt(topic);
            case "exam" -> buildExamPrompt(topic);
            case "checkin" -> buildCheckinPrompt(topic);
            default -> build21DayPrompt(topic);
        };

        var messages = List.of(
                com.volcengine.ark.runtime.model.completion.chat.ChatMessage.builder()
                        .role(ChatMessageRole.USER)
                        .content(prompt)
                        .build()
        );

        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(modelId)
                .messages(messages)
                .build();

        StringBuilder sb = new StringBuilder();
        arkService.createChatCompletion(request)
                .getChoices()
                .forEach(c -> sb.append(c.getMessage().getContent()));
        return sb.toString();
    }

    private String build21DayPrompt(String topic) {
        return """
            你是一个学习规划专家。用户想要在21天内入门一个新兴趣。
            请为以下主题制定一个完整的21天入门计划：""" + topic + """
            
            请严格按照以下Markdown格式输出：
            
            ### 元信息
            - 主题：xxx
            - 周期：21天
            - 难度：入门
            - 每日预计时长：30-60分钟
            
            ### Day 1-3: 认知与准备
            Day 1: xxx
            Day 2: xxx
            Day 3: xxx
            
            ### Day 4-7: 基础入门
            Day 4: xxx
            ...（以此类推到 Day 21）
            
            ### 学习建议
            给出3-4条实用建议
            """;
    }

    private String buildSpeedrunPrompt(String topic) {
        return """
            你是一个高效学习规划专家。用户想要在较短周期内掌握一门课程。
            请为以下课程制定一个速通学习计划：""" + topic + """
            
            请严格按照以下Markdown格式输出：
            
            ### 元信息
            - 课程：xxx
            - 推荐周期：xxx天
            - 难度：xxx
            - 每日预计时长：1.5-3小时
            
            ### 学习阶段（约每5-7天一个阶段）
            ...分阶段列出每天任务...
            
            ### 核心知识点清单
            列出必须掌握的10个核心知识点
            
            ### 推荐学习资源
            给出资源类型建议
            """;
    }

    private String buildExamPrompt(String topic) {
        return """
            你是一个备考规划专家。用户想要备考一个考试。
            请为以下考试制定备考计划：""" + topic + """
            
            请严格按照以下Markdown格式输出：
            
            ### 元信息
            - 考试：xxx
            - 建议备考周期：xxx天
            - 难度：xxx
            
            ### 备考阶段
            ...分阶段列出每天/每周任务...
            
            ### 重点复习清单
            按优先级列出重点内容
            
            ### 模拟测试建议
            给出刷题数量和频率建议
            """;
    }

    private String buildCheckinPrompt(String topic) {
        return """
            你是一个习惯养成专家。用户想要养成一个日常习惯。
            请为以下习惯制定打卡计划：""" + topic + """
            
            请严格按照以下Markdown格式输出：
            
            ### 元信息
            - 习惯：xxx
            - 周期：30天
            - 每日预计时长：xxx
            
            ### 每周递进计划
            ...按周分解...
            
            ### 打卡小技巧
            给出坚持的建议
            """;
    }
}
