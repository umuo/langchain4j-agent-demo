package com.example.agent.app;

import com.example.agent.core.OpenAiChatModelFactory;
import com.example.agent.domain.OrderRepository;
import com.example.agent.tools.OrderTools;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecifications;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import java.util.ArrayList;
import java.util.List;

/** 手动完成模型 → 工具请求 → 执行工具 → 回填结果 → 模型的循环。 */
public class ProgrammaticToolsDemo {
    private static final ObjectMapper JSON = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        var model = OpenAiChatModelFactory.getModel();
        var tools = new OrderTools(new OrderRepository());
        var specifications = ToolSpecifications.toolSpecificationsFrom(tools);
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(SystemMessage.from("你是中文订单客服。必须使用工具查询订单及退款资格，不编造数据，不执行退款。"));
        messages.add(UserMessage.from("查询 ORD-1001 和 ORD-1002 的商品及退款资格，并比较。"));

        // 允许最多5轮工具调用，再给模型一次生成最终答案的机会。
        for (int round = 0; round <= 5; round++) {
            var response = model.chat(ChatRequest.builder()
                .messages(messages).toolSpecifications(specifications).build());
            var aiMessage = response.aiMessage();
            messages.add(aiMessage);
            if (!aiMessage.hasToolExecutionRequests()) {
                System.out.println(aiMessage.text());
                return;
            }
            if (round == 5) throw new IllegalStateException("超过工具调用轮数限制");
            // 每个请求都必须回填对应结果；保留工具调用 ID。
            for (ToolExecutionRequest request : aiMessage.toolExecutionRequests()) {
                String result = execute(tools, request);
                System.out.println("调用工具：" + request.name() + "，结果：" + result);
                messages.add(ToolExecutionResultMessage.from(request, result));
            }
        }
    }

    private static String execute(OrderTools tools, ToolExecutionRequest request) throws Exception {
        var arguments = JSON.readTree(request.arguments());
        if (arguments == null || !arguments.isObject()
                || !arguments.path("orderId").isTextual()
                || arguments.path("orderId").asText().isBlank()) {
            return "工具参数错误：需要非空字符串 orderId";
        }
        String orderId = arguments.path("orderId").asText();
        // 显式白名单分派，不执行模型提供的任意方法。
        return switch (request.name()) {
            case "lookupOrder" -> tools.lookupOrder(orderId);
            case "checkRefundEligibility" -> tools.checkRefundEligibility(orderId);
            default -> "未知工具：" + request.name();
        };
    }
}
