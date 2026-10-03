package com.example.agent.app;

import com.example.agent.core.OpenAiChatModelFactory;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;

/** 基础 AI Service：接口代理自动处理输入、系统提示和输出。 */
public class AiServiceDemo {
    public interface Assistant {
        @SystemMessage("你是 Java 学习助手，使用中文简洁回答。")
        String chat(String message);
    }

    public static void main(String[] args) {
        Assistant assistant = AiServices.create(Assistant.class, OpenAiChatModelFactory.getModel());
        System.out.println(assistant.chat("用三句话解释 LangChain4j 的 AI Service。"));
    }
}
