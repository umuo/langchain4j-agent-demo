package com.example.agent.app;

import com.example.agent.core.AgentFactory;

/** AI Services 自动完成工具选择、执行、回填和会话记忆。 */
public class AiServiceToolsDemo {
    public static void main(String[] args) {
        var agent = AgentFactory.create();
        System.out.println(agent.chat("查询 ORD-1001 的商品、金额和退款资格。"));
        System.out.println(agent.chat("那 ORD-1002 呢？请比较两个订单的退款资格。"));
    }
}
