package com.example.financedashboard.mapper;

import com.example.financedashboard.entity.AiModelConfig;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * AI模型配置Mapper
 */
@Mapper
public interface AiModelConfigMapper {

    @Select("SELECT * FROM ai_model_config WHERE user_id = #{userId} AND status = 1")
    List<AiModelConfig> findByUserId(Long userId);

    @Select("SELECT * FROM ai_model_config WHERE id = #{id}")
    AiModelConfig findById(Long id);

    @Select("SELECT * FROM ai_model_config WHERE user_id = #{userId} AND is_default = 1 AND status = 1 LIMIT 1")
    AiModelConfig findDefaultByUserId(Long userId);

    @Insert("INSERT INTO ai_model_config(user_id, model_name, model_provider, api_key, api_endpoint, model_type, is_default, status, max_tokens, temperature) " +
            "VALUES(#{userId}, #{modelName}, #{modelProvider}, #{apiKey}, #{apiEndpoint}, #{modelType}, #{isDefault}, #{status}, #{maxTokens}, #{temperature})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AiModelConfig config);

    @Update("UPDATE ai_model_config SET model_name=#{modelName}, model_provider=#{modelProvider}, api_key=#{apiKey}, " +
            "api_endpoint=#{apiEndpoint}, model_type=#{modelType}, is_default=#{isDefault}, status=#{status}, " +
            "max_tokens=#{maxTokens}, temperature=#{temperature} WHERE id=#{id}")
    int update(AiModelConfig config);

    @Delete("DELETE FROM ai_model_config WHERE id = #{id}")
    int deleteById(Long id);

    @Update("UPDATE ai_model_config SET is_default = 0 WHERE user_id = #{userId}")
    int clearDefaultByUserId(Long userId);
}
