package com.example.financedashboard.controller;

import com.example.financedashboard.entity.AiModelConfig;
import com.example.financedashboard.mapper.AiModelConfigMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * AI模型配置管理接口
 */
@RestController
@RequestMapping("/api/ai-models")
@RequiredArgsConstructor
public class AiModelConfigController {

    private final AiModelConfigMapper modelConfigMapper;

    @GetMapping("/user/{userId}")
    public List<AiModelConfig> getUserModels(@PathVariable Long userId) {
        return modelConfigMapper.findByUserId(userId);
    }

    @PostMapping
    public Map<String, Object> addModel(@RequestBody ModelConfigRequest request) {
        if (request.getIsDefault() == 1) {
            modelConfigMapper.clearDefaultByUserId(request.getUserId());
        }

        AiModelConfig config = new AiModelConfig();
        config.setUserId(request.getUserId());
        config.setModelName(request.getModelName());
        config.setModelProvider(request.getModelProvider());
        config.setApiKey(request.getApiKey());
        config.setApiEndpoint(request.getApiEndpoint());
        config.setModelType(request.getModelType());
        config.setIsDefault(request.getIsDefault());
        config.setStatus(1);
        config.setMaxTokens(request.getMaxTokens());
        config.setTemperature(request.getTemperature());

        modelConfigMapper.insert(config);
        return Map.of("id", config.getId(), "message", "添加成功");
    }

    @PutMapping("/{id}")
    public Map<String, String> updateModel(@PathVariable Long id, @RequestBody ModelConfigRequest request) {
        AiModelConfig config = modelConfigMapper.findById(id);
        if (config == null) {
            throw new RuntimeException("模型配置不存在");
        }

        if (request.getIsDefault() == 1) {
            modelConfigMapper.clearDefaultByUserId(config.getUserId());
        }

        config.setModelName(request.getModelName());
        config.setModelProvider(request.getModelProvider());
        config.setApiKey(request.getApiKey());
        config.setApiEndpoint(request.getApiEndpoint());
        config.setModelType(request.getModelType());
        config.setIsDefault(request.getIsDefault());
        config.setMaxTokens(request.getMaxTokens());
        config.setTemperature(request.getTemperature());

        modelConfigMapper.update(config);
        return Map.of("message", "更新成功");
    }

    @DeleteMapping("/{id}")
    public Map<String, String> deleteModel(@PathVariable Long id) {
        modelConfigMapper.deleteById(id);
        return Map.of("message", "删除成功");
    }

    @Data
    static class ModelConfigRequest {
        private Long userId;
        private String modelName;
        private String modelProvider;
        private String apiKey;
        private String apiEndpoint;
        private String modelType;
        private Integer isDefault;
        private Integer maxTokens;
        private BigDecimal temperature;
    }
}
