package com.example.financedashboard.controller;

import com.example.financedashboard.dto.LLMRequest;
import com.example.financedashboard.service.LLMService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 大模型接口
 */
@RestController
@RequestMapping("/api/llm")
@RequiredArgsConstructor
public class LLMController {

    private final LLMService llmService;

    @PostMapping("/chat")
    public Map<String, String> chat(@RequestBody ChatRequest request) {
        Long userId = request.getUserId() != null ? request.getUserId() : 1L;
        String response = llmService.chat(userId, request.getMessage(), request.getImageUrl());
        return Map.of("response", response);
    }

    @PostMapping("/chat/model/{modelId}")
    public Map<String, String> chatWithModel(@PathVariable Long modelId, @RequestBody MultiChatRequest request) {
        String response = llmService.chat(modelId, request.getMessages());
        return Map.of("response", response);
    }

    @Data
    static class ChatRequest {
        private Long userId;
        private String message;
        private String imageUrl;
    }

    @Data
    static class MultiChatRequest {
        private List<LLMRequest.Message> messages;
    }
}
