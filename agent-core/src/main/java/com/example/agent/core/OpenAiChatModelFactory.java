package com.example.agent.core;

import dev.langchain4j.model.openai.OpenAiChatModel;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Properties;

/** 所有 Demo 共用的无参数模型工厂。 */
public final class OpenAiChatModelFactory {
    private OpenAiChatModelFactory() {}

    public static OpenAiChatModel getModel() {
        Properties properties = new Properties();
        // local 文件用于保存本机配置，优先于共享配置且不提交 Git。
        load(properties, "/openai.properties");
        load(properties, "/openai-local.properties");
        String apiKey = value(properties, "openai.api-key", "OPENAI_API_KEY");
        if (apiKey.isBlank() || apiKey.equals("replace-me")) {
            throw new IllegalStateException("请在 openai-local.properties 配置 openai.api-key，或设置 OPENAI_API_KEY");
        }
        return OpenAiChatModel.builder()
            .logRequests(Boolean.TRUE)
            .apiKey(apiKey)
            .baseUrl(value(properties, "openai.base-url", "OPENAI_BASE_URL"))
            .modelName(value(properties, "openai.model", "OPENAI_MODEL"))
            .temperature(Double.parseDouble(properties.getProperty("openai.temperature", "0.0")))
            .timeout(Duration.ofSeconds(Long.parseLong(properties.getProperty("openai.timeout-seconds", "60"))))
            .maxRetries(Integer.parseInt(properties.getProperty("openai.max-retries", "1")))
            .build();
    }

    private static void load(Properties properties, String resource) {
        try (InputStream input = OpenAiChatModelFactory.class.getResourceAsStream(resource)) {
            if (input != null) properties.load(input);
        } catch (IOException e) {
            throw new IllegalStateException("无法读取模型配置：" + resource, e);
        }
    }

    private static String value(Properties properties, String property, String environment) {
        String override = System.getenv(environment);
        return override == null || override.isBlank()
            ? properties.getProperty(property, "").trim() : override.trim();
    }
}
