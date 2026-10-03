package com.example.agent.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Order(String id, String product, BigDecimal amount, LocalDate deliveredOn) {}
