package com.cogniflow.service;

import com.cogniflow.entity.ChatMessage;
import com.cogniflow.repository.ChatMessageRepository;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import com.volcengine.ark.runtime.service.ArkService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChatService {

    private final ArkService arkService;
    private final ChatMessageRepository chatMessageRepository;

    @Value("${ark.model.id}")
    private String modelId;

    public ChatService(ArkService arkService, ChatMessageRepository chatMessageRepository) {
        this.arkService = arkService;
        this.chatMessageRepository = chatMessageRepository;
    }

    public String sendMessage(Long userId, String userMessage) {
        chatMessageRepository.save(new ChatMessage(userId, "user", userMessage));

        List<ChatMessage> history = chatMessageRepository.findByUserIdOrderByCreatedAtAsc(userId);

        List<com.volcengine.ark.runtime.model.completion.chat.ChatMessage> messages = new ArrayList<>();
        messages.add(com.volcengine.ark.runtime.model.completion.chat.ChatMessage.builder()
                .role(ChatMessageRole.SYSTEM)
                .content("你是 CogniFlow 智流学习导航助手。用热情、鼓励的语气回答，用 Markdown 格式回复（标题##、列表-、加粗**等），让内容条理清晰。当用户请求制定学习计划时，请按固定格式输出：先用###元信息标注标题/时间/难度，然后用###阶段拆分成每天的任务，每天一行\"Day N: 任务描述\"。用户问你一般性问题就正常聊天，不用强行给计划。")
                .build());

        for (ChatMessage msg : history) {
            ChatMessageRole role = "user".equals(msg.getRole()) ? ChatMessageRole.USER : ChatMessageRole.ASSISTANT;
            messages.add(com.volcengine.ark.runtime.model.completion.chat.ChatMessage.builder()
                    .role(role)
                    .content(msg.getContent())
                    .build());
        }

        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(modelId)
                .messages(messages)
                .build();

        StringBuilder replyBuilder = new StringBuilder();
        arkService.createChatCompletion(request)
                .getChoices()
                .forEach(choice -> replyBuilder.append(choice.getMessage().getContent()));
        String reply = replyBuilder.toString();

        chatMessageRepository.save(new ChatMessage(userId, "assistant", reply));

        return reply;
    }

    public List<ChatMessage> getHistory(Long userId) {
        return chatMessageRepository.findByUserIdOrderByCreatedAtAsc(userId);
    }
}
