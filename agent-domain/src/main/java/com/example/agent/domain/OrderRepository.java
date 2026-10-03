package com.example.agent.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

/** 内存演示数据；日期相对今天生成，方便持续演示退款资格。 */
public final class OrderRepository {
    private final Map<String, Order> orders = Map.of(
        "ORD-1001", new Order("ORD-1001", "机械键盘", new BigDecimal("399.00"), LocalDate.now().minusDays(3)),
        "ORD-1002", new Order("ORD-1002", "显示器", new BigDecimal("1499.00"), LocalDate.now().minusDays(15)));
    public Optional<Order> findById(String id) { return Optional.ofNullable(orders.get(id)); }
}
