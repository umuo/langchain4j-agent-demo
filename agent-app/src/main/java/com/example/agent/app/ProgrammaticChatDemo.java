package com.example.agent.app;

import com.example.agent.core.OpenAiChatModelFactory;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ChatRequest;

/** 编程式调用：显式构造消息和请求，直接处理 ChatResponse。 */
public class ProgrammaticChatDemo {
    public static void main(String[] args) {
        var model = OpenAiChatModelFactory.getModel();
        var request = ChatRequest.builder()
            .messages(SystemMessage.from("你是 Java 学习助手，使用中文简洁回答。"),
                      UserMessage.from("用三句话解释 LangChain4j 的 ChatModel。"))
            .build();
        var response = model.chat(request);
        System.out.println(response.aiMessage().text());
        System.out.println("Token 使用：" + response.tokenUsage());
    }
}
