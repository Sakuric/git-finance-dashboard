package com.example.financedashboard.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI模型配置实体类
 * 对应数据库表: ai_model_config
 * 用于存储用户自定义的AI模型配置信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AiModelConfig {
    private Long id;                    // 模型配置ID，主键
    private Long userId;                // 用户ID，外键关联user_info表
    private String modelName;           // 模型名称（DeepSeek/GPT-4/文心/通义）
    private String modelProvider;       // 服务提供商
    private String apiKey;              // API密钥
    private String apiEndpoint;         // API端点URL
    private String modelType;           // 模型类型：chat/completion
    private Integer isDefault;          // 是否默认：0-否，1-是
    private Integer status;             // 状态：0-禁用，1-启用
    private Integer maxTokens;          // 最大Token数
    private BigDecimal temperature;     // 温度参数
    private LocalDateTime createdAt;    // 创建时间
    private LocalDateTime updatedAt;    // 更新时间
}