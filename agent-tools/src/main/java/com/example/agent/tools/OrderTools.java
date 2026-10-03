package com.example.agent.tools;

import com.example.agent.domain.OrderRepository;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** 只读工具，不执行实际退款。 */
public final class OrderTools {
    private final OrderRepository repository;
    public OrderTools(OrderRepository repository) { this.repository = repository; }

    @Tool("查询演示订单详情，包括商品、金额和签收日期")
    public String lookupOrder(@P("订单号，例如 ORD-1001") String orderId) {
        return repository.findById(orderId)
            .map(o -> "订单=%s，商品=%s，金额=%s元，签收日期=%s".formatted(o.id(), o.product(), o.amount(), o.deliveredOn()))
            .orElse("订单不存在：" + orderId);
    }

    @Tool("检查订单退款资格：签收后7天内（含第7天）可以申请退款；不执行退款")
    public String checkRefundEligibility(@P("订单号") String orderId) {
        return repository.findById(orderId).map(o -> {
            long days = ChronoUnit.DAYS.between(o.deliveredOn(), LocalDate.now());
            return "订单=%s，签收天数=%d，可申请退款=%s；此工具仅查询资格。".formatted(orderId, days, days >= 0 && days <= 7);
        }).orElse("订单不存在：" + orderId);
    }
}
