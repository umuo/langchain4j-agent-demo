package com.example.agent.core;

import com.example.agent.domain.OrderRepository;
import com.example.agent.tools.OrderTools;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.tool.ToolArgumentsErrorHandler;
import dev.langchain4j.service.tool.ToolExecutionErrorHandler;

public final class AgentFactory {
    private AgentFactory() {}
    /** 每次创建一个独立会话；不要跨用户共享此对象或并发调用。 */
    public static CustomerSupportAgent create() {
        return AiServices.builder(CustomerSupportAgent.class)
            .chatModel(OpenAiChatModelFactory.getModel())
            .chatMemory(MessageWindowChatMemory.withMaxMessages(20))
            .tools(new OrderTools(new OrderRepository()))
            // 参数错误反馈给模型修正；普通工具异常终止调用，避免暴露内部异常信息。
            .toolArgumentsErrorHandler(ToolArgumentsErrorHandler.sendExceptionMessageToLlm())
            .toolExecutionErrorHandler(ToolExecutionErrorHandler.failInvocationUnlessVisibleToLlm())
            .maxSequentialToolsInvocations(5)
            .build();
    }
}
