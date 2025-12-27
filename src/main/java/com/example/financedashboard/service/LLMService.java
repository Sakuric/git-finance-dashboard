package com.example.financedashboard.service;

import com.alibaba.fastjson2.JSON;
import com.example.financedashboard.dto.LLMRequest;
import com.example.financedashboard.dto.LLMResponse;
import com.example.financedashboard.entity.AiModelConfig;
import com.example.financedashboard.mapper.AiModelConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 大模型服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LLMService {

    private final AiModelConfigMapper modelConfigMapper;
    private final OkHttpClient baseClient;

    /**
     * 调用大模型（使用用户默认配置）
     */
    public String chat(Long userId, String userMessage, String imageUrl) {
        AiModelConfig config = modelConfigMapper.findDefaultByUserId(userId);
        if (config == null) {
            throw new RuntimeException("未找到默认模型配置");
        }

        LLMRequest.Message message;
        if (imageUrl != null && !imageUrl.isEmpty()) {
            message = new LLMRequest.Message("user", userMessage, imageUrl);
        } else {
            message = new LLMRequest.Message("user", userMessage);
        }

        return chat(config.getId(), List.of(message));
    }

    /**
     * 调用大模型（指定模型ID）
     */
    public String chat(Long modelId, List<LLMRequest.Message> messages) {
        AiModelConfig config = modelConfigMapper.findById(modelId);
        if (config == null) {
            throw new RuntimeException("模型配置不存在");
        }

        LLMRequest request = new LLMRequest();
        request.setModel(config.getModelName());
        request.setMessages(messages);
        request.setMaxTokens(config.getMaxTokens());
        request.setTemperature(config.getTemperature().doubleValue());

        return callLLM(config, request);
    }

    private String callLLM(AiModelConfig config, LLMRequest request) {
        OkHttpClient client = baseClient.newBuilder()
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        RequestBody body = RequestBody.create(
                JSON.toJSONString(request),
                MediaType.parse("application/json")
        );

        Request httpRequest = new Request.Builder()
                .url(config.getApiEndpoint() + "/chat/completions")
                .addHeader("Authorization", "Bearer " + config.getApiKey())
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        try (Response response = client.newCall(httpRequest).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("LLM调用失败: " + response.code());
            }

            String responseBody = response.body().string();
            LLMResponse llmResponse = JSON.parseObject(responseBody, LLMResponse.class);

            if (llmResponse.getChoices() == null || llmResponse.getChoices().isEmpty()) {
                throw new RuntimeException("LLM返回格式异常");
            }

            String content = llmResponse.getChoices().get(0).getMessage().getContent();

            // 验证content是否为有效JSON
            try {
                JSON.parseObject(content);
                return content;
            } catch (Exception e) {
                throw new RuntimeException("LLM返回的content不是有效JSON");
            }
        } catch (Exception e) {
            log.error("调用大模型失败", e);
            throw new RuntimeException("调用大模型失败: " + e.getMessage());
        }
    }
}
