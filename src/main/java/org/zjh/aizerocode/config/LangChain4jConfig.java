package org.zjh.aizerocode.config;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * LangChain4j 配置类
 * 配置长超时以支持 AI 代码生成等耗时操作
 *
 * @author kayson
 */
@Configuration
public class LangChain4jConfig {

    @Value("${langchain4j.open-ai.chat-model.base-url}")
    private String baseUrl;

    @Value("${langchain4j.open-ai.chat-model.api-key}")
    private String apiKey;

    @Value("${langchain4j.open-ai.chat-model.model-name}")
    private String modelName;

    @Value("${langchain4j.open-ai.chat-model.max-tokens:8192}")
    private Integer maxTokens;

    @Bean
    public StreamingChatModel reasoningStreamingChatModel() {
        // 生产环境使用 Reasoning 模型
        // final String modelName = "";
        // final int maxTokens = 32768;
        return OpenAiStreamingChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .maxTokens(maxTokens)
                .timeout(Duration.ofMinutes(5)) // 5分钟超时
                .build();
    }
}
