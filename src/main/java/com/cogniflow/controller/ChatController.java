package com.cogniflow.controller;

import com.cogniflow.entity.ChatMessage;
import com.cogniflow.service.ChatService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/send")
    public Map<String, String> send(@RequestBody Map<String, Object> body) {
        Long userId = toLong(body.get("userId"));
        String message = (String) body.get("message");
        String reply = chatService.sendMessage(userId, message);
        return Map.of("reply", reply);
    }

    @GetMapping("/history")
    public List<ChatMessage> history(@RequestParam Long userId) {
        return chatService.getHistory(userId);
    }

    private Long toLong(Object val) {
        if (val instanceof Number n) return n.longValue();
        return Long.parseLong(val.toString());
    }
}
